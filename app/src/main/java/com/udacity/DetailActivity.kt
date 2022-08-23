package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.Constants.FILE_NAME_KEY
import com.udacity.Constants.STATUS_KEY
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var bindingContent: ContentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        bindingContent = binding.contentDetail

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        bindingContent.fileName.text = intent.getStringExtra(FILE_NAME_KEY)
        bindingContent.status.text = intent.getStringExtra(STATUS_KEY)

        if (intent.getStringExtra(STATUS_KEY) == getString(R.string.failed_status))
            bindingContent.status.setTextColor(Color.RED)
        else
            bindingContent.status.setTextColor(Color.GREEN)

        bindingContent.btnOk.setOnClickListener {
            finish()
        }
    }
}