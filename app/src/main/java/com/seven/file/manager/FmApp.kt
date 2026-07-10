package com.seven.file.manager

import com.seven.basis.BasisApplication
import com.seven.basis.timberTool.TimberTool

class FmApp : BasisApplication() {

    override fun onMain() {
        super.onMain()
        TimberTool.iArgs("File Manager Application On Main")
    }
}