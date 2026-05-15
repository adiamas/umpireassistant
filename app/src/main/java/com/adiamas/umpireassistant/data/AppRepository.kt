package com.adiamas.umpireassistant.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase, private val sessionDb: SessionDatabase) {

    val configs: Flow<List<StoredConfigEntity>> = db.storedConfigDao().getAllConfigs()
    val session: Flow<AppSessionEntity?> = sessionDb.appSessionDao().getSession()

    fun getTeamsForConfig(configId: Int): Flow<List<TeamEntity>> =
        db.teamDao().getTeamsForConfig(configId)

    suspend fun getConfigById(id: Int): StoredConfigEntity? = db.storedConfigDao().getById(id)
    suspend fun getConfigByName(name: String): StoredConfigEntity? = db.storedConfigDao().getByName(name)
    suspend fun getDefaultConfig(): StoredConfigEntity? = db.storedConfigDao().getDefault()

    suspend fun insertConfig(config: StoredConfigEntity): Long = db.storedConfigDao().insert(config)
    suspend fun updateConfig(config: StoredConfigEntity) = db.storedConfigDao().update(config)
    suspend fun deleteConfig(config: StoredConfigEntity) = db.storedConfigDao().delete(config)

    suspend fun addTeam(team: TeamEntity) = db.teamDao().insert(team)
    suspend fun updateTeam(team: TeamEntity) = db.teamDao().update(team)
    suspend fun deleteTeam(team: TeamEntity) = db.teamDao().delete(team)

    suspend fun saveSession(session: AppSessionEntity) = sessionDb.appSessionDao().save(session)

    suspend fun ensureDefaultConfig(): Int {
        val existing = db.storedConfigDao().getDefault()
        if (existing != null) return existing.id
        return db.storedConfigDao().insert(
            StoredConfigEntity(name = "Default", isDefault = true)
        ).toInt()
    }
}
