package com.example.imagefilter.fragments

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.IpCons
import com.esafirm.imagepicker.model.Image
import com.example.imagefilter.R
import com.example.imagefilter.transformations.DitherTransformation
import com.robertlevonyan.components.picker.PickerDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_filter_image_crop.*
import java.io.File
import java.io.OutputStream


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FilterImageAndCropFragment : Fragment() {

    private var tempFileUri: Uri? = null
    private var image: Image? = null
    private var pickerDialog: PickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_image_crop, container, false)
    }

    private val TAG = "FilterImageAndCropFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Picasso.get().load(R.drawable.demo).transform(transformation).into((ImageView) findViewById (R.id.image));
        val mIcon = BitmapFactory.decodeResource(requireContext().resources, R.drawable.lena)

        hideImageNoLoader()

        fabButton?.setOnClickListener {
            Log.d(TAG, "Button clicked")

            hideImageWithLoader()

            ImagePicker
                .create(this)
                .single()
                .limit(1)
                .start()
        }

        button_first.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "OnActivityResult $requestCode, $resultCode, ${data?.data}")

        when (requestCode) {
            IpCons.RC_IMAGE_PICKER -> {
                image = ImagePicker.getFirstImageOrNull(data)
                if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                    image?.uri?.let {
                        Log.e(TAG, "RESULT_OK ok $it")

                        hideImageWithLoader()
                        tempFileUri = makeTempFile()

                        Toast.makeText(
                            requireContext(),
                            "You added ${image?.name} images",
                            Toast.LENGTH_LONG
                        ).show()

                        performCrop(it)
                        return
                    }
                } else if (resultCode != RESULT_OK) {
                    Log.e(TAG, "RESULT_Not ok")
                    hideImageNoLoader()
                    image = null
                }
            }
            Companion.PIC_CROP -> {
                if (resultCode == RESULT_OK) {
                    val uri: Uri? = data?.data

                    if (uri != null) {
                        Log.e(TAG, "Filtering image after crop")
                        Toast.makeText(
                            requireContext(),
                            "Filtering image after crop",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadBitmapByPicasso(uri)
                        showImage()
                    } else {
                        Log.e(TAG, "Crop failed please try again")
                        Toast.makeText(
                            requireContext(),
                            "Crop failed please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideImageNoLoader()
                    }
                }
            }
        }
    }

    private fun loadBitmapByPicasso(pBitmap: Uri, pImageView: ImageView = imageFiltered) {

        try {
            hideImageWithLoader()

            Picasso.get().load(pBitmap).transform(DitherTransformation()).into(pImageView)
            imageFiltered.visibility = View.VISIBLE

        } catch (e: Exception) {
            //outputStream?.close()
            Log.e(TAG, "LoadBitmapByPicasso ${e.message ?: "null bitmap"}")
        }
    }

    private fun loadBitmapByPicasso(pBitmap: Bitmap, pImageView: ImageView = imageFiltered) {
        var outputStream: OutputStream? = null

        try {
            val uri: Uri = makeTempFile()
            hideImageWithLoader()
            outputStream = requireContext().contentResolver.openOutputStream(uri)
            (pBitmap as Bitmap).compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream?.close()
            Picasso.get().load(uri).transform(DitherTransformation()).into(pImageView)
        } catch (e: Exception) {
            outputStream?.close()
            Log.e("LoadBitmapByPicasso", e.message ?: "null bitmap")
        }
    }

    private fun makeTempFile() = Uri.fromFile(
        File.createTempFile(
            "temp_file_name",
            ".jpg",
            requireContext().cacheDir
        )
    )

    private fun showImage() {
        progress_bar.visibility = View.GONE
        textview_first.visibility = View.GONE
        imageFiltered.visibility = View.VISIBLE
    }

    private fun hideImageNoLoader() {
        progress_bar.visibility = View.GONE
        textview_first.visibility = View.VISIBLE
        imageFiltered.visibility = View.GONE
    }

    private fun hideImageWithLoader() {
        progress_bar.visibility = View.VISIBLE
        textview_first.visibility = View.GONE
        imageFiltered.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pickerDialog?.onPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun performCrop(imageUri: Uri) {
        try {
            val intent = Intent("com.android.camera.action.CROP")
            intent.type = "image/*"
            val list: List<ResolveInfo> =
                requireActivity().packageManager.queryIntentActivities(intent, 0)
            val size = list.size
            if (size >= 0) {
                intent.data = imageUri
                intent.putExtra("crop", "false")
                intent.putExtra("aspectX", 1)
                intent.putExtra("aspectY", 1)
                intent.putExtra("scale", true)
                intent.putExtra("return-data", true)
                val i = Intent(intent)
                val res = list[0]
                i.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)

                startActivityForResult(i, Companion.PIC_CROP)
            }
        } catch (anfe: ActivityNotFoundException) {
            val errorMessage = "Whoops - your device doesn't support the crop action!" +
                    "\n filter whole photo"
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            loadBitmapByPicasso(imageUri)
        }
    }

    companion object {
        private const val PIC_CROP: Int = 2222
    }
}