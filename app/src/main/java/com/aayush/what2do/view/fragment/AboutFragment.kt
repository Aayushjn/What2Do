package com.aayush.what2do.view.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aayush.what2do.R
import com.aayush.what2do.util.android.toast
import kotlinx.android.synthetic.main.fragment_about.*


class AboutFragment: BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_about, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:aayushjn11@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "")
                putExtra(Intent.EXTRA_TEXT, "")
            }

            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                startActivity(intent)
            } else {
                context?.toast("Unable to find email apps!")
            }
        }

        text_github.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.github.com/Aayushjn/What2Do")
            }

            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic fun newInstance(): AboutFragment = AboutFragment()
    }
}

