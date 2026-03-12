package com.myaux.app.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager

object DatabaseConnection {

    private const val URL =
        "jdbc:mysql://students-data-mysql-studentsdatamysql.c.aivencloud.com:11529/students_data_mysql" +
                "?useSSL=true" +
                "&requireSSL=true" +
                "&verifyServerCertificate=false" +
                "&serverTimezone=UTC" +
                "&connectTimeout=8000" +
                "&socketTimeout=8000"

    private const val USER = "avnadmin"
    private const val PASSWORD = "AVNS_X8ZLHz18HP2o0JB2eGv"

    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(URL, USER, PASSWORD)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
