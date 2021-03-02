package com.github.monun.survival.plugin

import com.github.monun.survival.Config
import com.github.monun.survival.Survival
import com.github.monun.tap.fake.FakeEntityServer
import java.io.File
import java.util.logging.Logger

class TickTask(
    private val logger: Logger,
    private val configFile: File,
    private val fakeEntityServerForZombie: FakeEntityServer,
    private val fakeEntityServerForHuman: FakeEntityServer,
    private val survival: Survival
): Runnable {
    private var configFileLastModified = configFile.lastModified()

    override fun run() {
        for (player in survival.players) {
            player.update()
        }

        fakeEntityServerForZombie.update()
        fakeEntityServerForHuman.update()

        if (configFileLastModified != configFile.lastModified()) {
            Config.load(configFile)
            configFileLastModified = configFile.lastModified()

            survival.players.forEach { it.bio.applyAttribute() }

            logger.info("Config reloaded")
        }
    }
}