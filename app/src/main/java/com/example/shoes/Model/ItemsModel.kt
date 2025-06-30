package com.example.shoes.Model

import android.os.Parcel
import android.os.Parcelable

data class ItemsModel(
    var title:String="",
    var description:String="",
    var picUrl:ArrayList<String> = ArrayList(),
    var size:ArrayList<String> = ArrayList(),
    var brand:String="",
    var price:Double=0.0,
    var rating:Double=0.0,
    var numberInCart:Int=0
):Parcelable{
    constructor(parcel: Parcel):this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(description)
        dest.writeStringList(picUrl)
        dest.writeStringList(size)
        dest.writeString(brand)
        dest.writeDouble(price)
        dest.writeDouble(rating)
        dest.writeInt(numberInCart)
    }

    companion object CREATOR : Parcelable.Creator<ItemsModel> {
        override fun createFromParcel(parcel: Parcel): ItemsModel {
            return ItemsModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsModel?> {
            return arrayOfNulls(size)
        }
    }
}
