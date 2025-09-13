package com.example.pricing

fun main(){

}

class PriceCalculator(val taxRate: Double){
    fun total(netAmount: Double): Double{
        if(netAmount <= 0) throw IllegalArgumentException("Net amount must be greater")
        return netAmount * (1.0 + taxRate)
    }
}
