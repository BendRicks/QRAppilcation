package ru.bendricks.qrappilcation.ui.scanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import ru.bendricks.qrappilcation.databinding.FragmentScannerBinding

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private lateinit var codeScanner: CodeScanner
    private var clipboardManager: ClipboardManager? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)

        clipboardManager = getSystemService(this.requireContext(), ClipboardManager::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = binding.scannerView
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            val data = it.text
            val uri = Uri.parse(data)
            if (uri.isHierarchical && uri.isAbsolute){
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data)
                startActivity(intent)
            } else {
                val clipData = ClipData.newPlainText("QR-Code data", data)
                clipboardManager?.setPrimaryClip(clipData)
                activity.runOnUiThread {
                    Toast.makeText(
                        this.requireContext(),
                        data.plus(" copied to clipboard"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}