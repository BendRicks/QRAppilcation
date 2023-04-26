package ru.bendricks.qrappilcation.ui.generator

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.encoder.QRCode
import com.journeyapps.barcodescanner.BarcodeEncoder
import ru.bendricks.qrappilcation.databinding.FragmentGeneratorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.Hashtable

class GeneratorFragment : Fragment() {

    private var _binding: FragmentGeneratorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var valueToCode: EditText
    private lateinit var generateButton: Button
    private lateinit var qrImageView: ImageView
    private lateinit var shareButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val generatorViewModel =
            ViewModelProvider(this).get(GeneratorViewModel::class.java)

        _binding = FragmentGeneratorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        valueToCode = binding.valueToCode
        generateButton = binding.generateButton
        shareButton = binding.shareButton
        qrImageView = binding.qrCodeView

        generatorViewModel.bitmap.observe(viewLifecycleOwner) {
            qrImageView.setImageBitmap(it)
        }

        generateButton.setOnClickListener {
            val qrValue = valueToCode.text.toString().trim()
            if (qrValue.isBlank()) {
                Toast.makeText(this.context, "Value is empty", Toast.LENGTH_SHORT).show()
            } else {
                val qrWriter = QRCodeWriter()
                try {
                    val hints = HashMap<EncodeHintType, String>()
                    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
                    val matrix = qrWriter.encode(qrValue, BarcodeFormat.QR_CODE, 400, 400, hints)
                    generatorViewModel.updateBitmap(BarcodeEncoder().createBitmap(matrix))
                    getSystemService(this.requireContext(), InputMethodManager::class.java)
                        ?.hideSoftInputFromWindow(valueToCode.applicationWindowToken, 0)
                } catch (ex: WriterException) {
                    Toast.makeText(this.context, "Error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

        shareButton.setOnClickListener {
            val intent = Intent()
            if (isExternalStorageWriteable()) {
                val uri = saveImageExternal(qrImageView.drawToBitmap())
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/png"
                startActivity(intent)
            } else {
                Toast.makeText(this.context, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun saveImageExternal(image: Bitmap): Uri{
        lateinit var file: File
        try {
            file = File(getExternalFilesDirs(this.requireContext(), Environment.DIRECTORY_PICTURES)[0], "to-share.png")
            val fos = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: IOException) {
            Toast.makeText(this.context, "Error occurred", Toast.LENGTH_SHORT).show()
        }
        return FileProvider.getUriForFile(this.requireContext(), "ru.bendricks.qrappilcation.fileprovider", file)
    }

    private fun isExternalStorageWriteable(): Boolean {
        val state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED == state) {
            return true;
        }
        return false;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}