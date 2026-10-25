package com.example.fintechpagoflex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Commitment::class, Payment::class, Recommendation::class, Reminder::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintech_database_v3"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.appDao()
                                    val now = System.currentTimeMillis()
                                    val dayMs = 86400000L

                                    dao.insertCommitments(
                                        listOf(
                                            Commitment(
                                                company = "Enel Distribución",
                                                concept = "Cuenta de Electricidad Mensual",
                                                amount = 32450.0,
                                                dueDate = now + dayMs * 5,
                                                status = "Pendiente",
                                                photoUri = null
                                            ),
                                            Commitment(
                                                company = "Aguas Andinas",
                                                concept = "Consumo de Agua Potable",
                                                amount = 18900.0,
                                                dueDate = now + dayMs * 10,
                                                status = "Pendiente",
                                                photoUri = null
                                            ),
                                            Commitment(
                                                company = "Movistar Fibra",
                                                concept = "Servicio de Internet Hogar",
                                                amount = 25990.0,
                                                dueDate = now - dayMs * 3,
                                                status = "Vencido",
                                                photoUri = null
                                            ),
                                            Commitment(
                                                company = "Gastos Comunes Edificio",
                                                concept = "Cuota Mensual Administración",
                                                amount = 85000.0,
                                                dueDate = now - dayMs * 7,
                                                status = "Vencido",
                                                photoUri = null
                                            )
                                        )
                                    )

                                    val santanderId = dao.insertCommitment(
                                        Commitment(
                                            company = "Banco Santander",
                                            concept = "Cuota Crédito de Consumo",
                                            amount = 145000.0,
                                            dueDate = now - dayMs * 15,
                                            status = "Pagado",
                                            photoUri = null
                                        )
                                    )

                                    dao.insertPayment(
                                        Payment(
                                            commitmentId = santanderId,
                                            amount = 145000.0,
                                            date = now - dayMs * 15,
                                            paymentMethod = "Transferencia"
                                        )
                                    )

                                    dao.insertRecommendations(
                                        listOf(
                                            Recommendation(
                                                organizationName = "Clínica Dental RedSalud",
                                                type = "Salud",
                                                reason = "Facilitar pago de consultas"
                                            ),
                                            Recommendation(
                                                organizationName = "Colegio San José",
                                                type = "Educación",
                                                reason = "Pago de mensualidades"
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}