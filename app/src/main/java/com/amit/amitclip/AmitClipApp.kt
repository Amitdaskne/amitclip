package com.amit.amitclip

import android.app.Application
import com.amit.amitclip.data.ClipDatabase
import com.amit.amitclip.data.ClipRepository

class AmitClipApp : Application() {
    val repository: ClipRepository by lazy {
        ClipRepository(ClipDatabase.get(this).clipDao())
    }
}
