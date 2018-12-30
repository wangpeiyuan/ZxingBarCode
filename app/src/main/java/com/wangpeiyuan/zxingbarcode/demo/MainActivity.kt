package com.wangpeiyuan.zxingbarcode.demo

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.wangpeiyuan.zxingbarcode.core.DecodeListener
import com.wangpeiyuan.zxingbarcode.core.EncodeListener
import com.wangpeiyuan.zxingbarcode.core.ZxingBarCode
import com.wangpeiyuan.zxingbarcode.core.ZxingBarCode.encodeQRCode
import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeParams
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val CHOOSE_PHOTO = 100
    private var decoderType = ZxingBarCode.DecodeType.Both
    private var mStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ZxingBarCode.setDebug(true)
        initListener()
    }

    fun initListener() {
        btnZBarScanner.setOnClickListener {
            decoderType = ZxingBarCode.DecodeType.ZBar
            openAlbum()
        }
        btnZXingScanner.setOnClickListener {
            decoderType = ZxingBarCode.DecodeType.ZXing
            openAlbum()
        }
        btnScanner.setOnClickListener {
            decoderType = ZxingBarCode.DecodeType.Both
            openAlbum()
        }
        btnGenerateQRCode.setOnClickListener {
            generateQRCode(buildQRCodeParams())
        }
    }

    fun openAlbum() {
        if (hasPermission()) {
            var intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, CHOOSE_PHOTO)
        } else {
            requestPermissions()
        }
    }


    fun hasPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this, "申请读写权限来获取图片",
            999,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        openAlbum()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        showToast("禁止了读写权限无法获取图片，请自行去设置中打开应用权限")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            handleImageOnKitkat(data)
        }
    }

    private fun handleImageOnKitkat(data: Intent) {
        var imagePath: String? = null
        val uri = data.data
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri!!.authority) {
                val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content:" + "//downloads/public_downloads"),
                    java.lang.Long.valueOf(docId)
                )
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.path
        }
        if (imagePath != null) {
            decodeQRCode(imagePath)
        }
    }

    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        //通过uri和selection来获取真实的图片路径
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    fun decodeQRCode(path: String) {
        val bitmap = ivBarCode.setImagePath(path)
        ZxingBarCode.setOnlyQRCode(true)
        mStartTime = System.currentTimeMillis()
        ZxingBarCode.decodeCodeBar(bitmap, null, true, object : DecodeListener {
            override fun onDecodeSuccess(resultList: MutableList<DecoderResult>?) {
                if (resultList == null || resultList.isEmpty()) {
                    onDecodeFail(Exception("无内容"))
                    return
                }
                var msg = "耗时: ${System.currentTimeMillis() - mStartTime}ms"
                var index = 1
                for (result in resultList) {
                    msg += "\n二维码 $index : 类型 = ${result.formatName}, 解码器 = ${result.decoderType}, 内容 = ${result.content}\n"
                    index++
                }
                showMessageDialog("二维码解析成功", msg)
                ivBarCode.setQRCodeDecoderResult(resultList)
            }

            override fun onDecodeFail(t: Throwable?) {
                showMessageDialog("二维码解析失败", t.toString())
            }
        }, decoderType)
    }

    fun buildQRCodeParams(): QRCodeParams {
        return QRCodeParams.Builder()
            .setContent("测试二维码生成")
            .setWidth(130)
            .setHeight(130)
//            .setPreColor(Color.BLUE)
            .setPreBitmap(BitmapFactory.decodeStream(resources.assets.open("pre.jpg")))
            .setLogoInfo(
                BitmapFactory.decodeStream(resources.assets.open("logo.png")),
                6, 6f
            )
            .setBackgroundInfo(
                BitmapFactory.decodeStream(resources.assets.open("bg.jpeg")),
                10, 10, 100
            )
            .build()
    }

    fun generateQRCode(params: QRCodeParams) {
        mStartTime = System.currentTimeMillis()
        encodeQRCode(params, object : EncodeListener {
            override fun onEncodeSuccess(bitmap: Bitmap?, outPutPath: String?) {
                if (bitmap == null) {
                    onEncodeFail(Exception("bitmap is null"))
                    return
                }
                showToast("二维码生成成功，耗时：${System.currentTimeMillis() - mStartTime}ms")
                ivBarCode.setImageBitmap(bitmap)
            }

            override fun onEncodeFail(t: Throwable?) {
                showMessageDialog("二维码生成失败", t.toString())
            }
        })
    }

    fun showMessageDialog(title: String, message: String) {
        val fragment = MessageDialogFragment.newInstance(title, message)
        fragment.show(supportFragmentManager, "scan_results")
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
