package tn.ahmi.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

const val CURRENT_USER_ID = 0

@Entity
data class User(
    var BleMac: String? = null,
    @SerializedName("startTime") var startTimeStamp: Long? = null,
    var avgDist: Double? = null,
    var minDist: Double? = null,
    var interractionTime: Long? = null
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = CURRENT_USER_ID
}