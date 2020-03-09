package com.harshabhadra.pdfsample2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var btn:Button? = null
    private var btnScroll: Button? = null
    private var llPdf: LinearLayout? = null
    private var bitmap: Bitmap? = null
    private lateinit var folder:File
    var targetPdf:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestReadPermissions()
        btn = findViewById(R.id.button_pdf)
        llPdf = findViewById(R.id.llpdf)
        val scrolll:ScrollView = findViewById(R.id.scroll_view)
        val openFileButton:Button = findViewById(R.id.open_file)

        openFileButton.setOnClickListener {
            openFile()
        }

        btn!!.setOnClickListener {
            folder = File(Environment.getExternalStorageDirectory(),"PdfSample")
            Log.e("MainActivity",folder.absolutePath)
            var success = true
            if(!folder.exists()){
                success = folder.mkdirs()
                Toast.makeText(this, "Folder created",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Folder exists",Toast.LENGTH_SHORT).show()
            }
            targetPdf = folder.absolutePath + "/" + "sample_pdf" + System.currentTimeMillis()+ ".pdf"
            Log.e("size", " " + llPdf!!.width + "  " + llPdf!!.width)
            bitmap = loadBitmapFromView(llPdf!!,llPdf!!.width,llPdf!!.height)
            createPdf()
        }
    }

    private fun createPdf(){
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //  Display display = wm.getDefaultDisplay();
        val displaymetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displaymetrics)
        val hight = displaymetrics.heightPixels.toFloat()
        val width = displaymetrics.widthPixels.toFloat()

        val convertHighet = hight.toInt()
        val convertWidth = width.toInt()

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo
            .Builder(convertWidth, convertHighet, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas

        val paint = Paint()
        canvas.drawPaint(paint)

        bitmap = Bitmap.createScaledBitmap(bitmap!!, convertWidth, convertHighet, true)

        paint.color = Color.BLUE
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        document.finishPage(page)

        // write the document content

        Log.d("target",targetPdf)
        val filePath: File
        filePath = File(targetPdf)
        try {
            document.writeTo(FileOutputStream(filePath))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Something wrong: $e", Toast.LENGTH_LONG).show()
        }

        // close the document
        document.close()
        Toast.makeText(this, "PDF is created!!!", Toast.LENGTH_SHORT).show()

    }

    fun openFile(){
//        val map = MimeTypeMap.getSingleton()
//        val ext = MimeTypeMap.getFileExtensionFromUrl(folder.name)
//        var type = map.getExtensionFromMimeType(ext)
//
//        if(type == null){
//            type = "*/*"
//        }
        val intent = Intent(Intent.ACTION_VIEW)
        val data = Uri.parse("file://" +targetPdf)
        intent.setDataAndType(data,"application/pdf")
        startActivity(Intent.createChooser(intent,"Open Pdf"))
    }

    companion object {

        fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap {
            val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.draw(c)

            return b
        }
    }

    private fun requestReadPermissions() {

        Dexter.withActivity(this)
            .withPermissions( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "All permissions are granted by user!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener(object : PermissionRequestErrorListener {
                override fun onError(error: DexterError) {
                    Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show()
                }
            })
            .onSameThread()
            .check()
    }
}
