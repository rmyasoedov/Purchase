package com.example.purchase

class ShoppinList{
    var annotName: String ? =null
    var annotCost: String ?=null

    constructor(annotName: String, annotCost: String){
        this.annotCost = annotCost
        this.annotName = annotName
    }
}