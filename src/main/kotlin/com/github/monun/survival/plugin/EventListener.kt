package com.github.monun.survival.plugin

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.github.monun.survival.Bio
import com.github.monun.survival.Survival
import com.github.monun.survival.SurvivalConfig
import com.github.monun.survival.survival
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.min
import kotlin.random.Random.Default.nextDouble

class EventListener(
    private val survival: Survival
) : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (!player.hasPlayedBefore()) {
            player.teleport(player.world.worldBorder.center.random(SurvivalConfig.worldSize / 2.0))
        }

        survival.loadPlayer(event.player)

        event.joinMessage(null)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        survival.unloadPlayer(event.player)

        event.quitMessage(null)
    }

    @EventHandler(ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncChatEvent) {
        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (!event.player.isOp) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreatePortal(event: PortalCreateEvent) {
        event.entity?.let { entity ->
            if (entity is Player && entity.survival().bio is Bio.Human) {
                return
            }
        }

        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val loc = event.respawnLocation
        event.respawnLocation = loc.world.worldBorder.center.random(SurvivalConfig.worldSize / 2.0)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage(null)
    }

    @EventHandler
    fun onServerListPing(event: PaperServerListPingEvent) {
        event.setHidePlayers(true)
        event.maxPlayers = 0
        event.numPlayers = 0
        event.motd(
            Component.text().color(TextColor.color(0xDD0000)).content("${ChatColor.BOLD}S U R V I V A L").build()
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FALL) return
        val entity = event.entity; if (entity !is LivingEntity) return
        val boots = entity.equipment?.boots

        var bootsDamage = event.damage * SurvivalConfig.bootsFallDamage

        if (boots != null) {
            val bootsMeta = boots.itemMeta

            if (bootsMeta is Damageable) {
                val maxDurability = boots.type.maxDurability
                val currentDurability = bootsMeta.damage
                val remainDurability = maxDurability - currentDurability
                val reduce = min(remainDurability, bootsDamage.toInt())

                bootsDamage -= reduce
                bootsMeta.damage += reduce
                boots.itemMeta = bootsMeta

                if (bootsMeta.damage >= maxDurability) {
                    boots.amount = 0
                }
            }
        }

        if (bootsDamage > 0) {
            entity.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOW,
                    (bootsDamage / SurvivalConfig.bootsFallDamage).toInt() * 20,
                    SurvivalConfig.bootsFallSlow, true, true, true
                )
            )
        }

        event.damage = 0.0
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryPickup(event: InventoryPickupItemEvent) {
        if (event.item.itemStack.type == Material.TOTEM_OF_UNDYING && event.inventory.type == InventoryType.HOPPER) {
            event.isCancelled = true
        }
    }
}

fun Location.random(spread: Double): Location {
    x += nextDouble(-spread, spread)
    z += nextDouble(-spread, spread)
    return toHighestLocation().add(0.5, 1.0, 0.5)
}