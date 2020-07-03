package tn.ahmi.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tn.ahmi.data.db.entities.CURRENT_USER_ID
import tn.ahmi.data.db.entities.User

@Dao
interface UserDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User) : Long

    @Query("SELECT * FROM user ORDER BY uid DESC LIMIT 1")
    fun getuser() : User?

    @Query("SELECT * FROM user")
    fun getuserData() : Array<User>

    @Query("SELECT * FROM user WHERE uid <= :lastId")
    fun getuserLastData(lastId: Int) : Array<User>

    @Query("DELETE FROM User WHERE uid <= :lastId")
    fun deleteAll(lastId: Int)
}