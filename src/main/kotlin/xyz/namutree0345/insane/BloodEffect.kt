package xyz.namutree0345.insane

import com.github.monun.survival.Bio
import com.github.monun.survival.Survival
import com.github.monun.survival.survival
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BloodEffect : Listener {

    @EventHandler
    fun bloodOnDamage(event: EntityDamageByEntityEvent) {
        if(event.damager is Player && event.entity is Player) {
            if((event.damager as Player).survival().bio.type.toString().contains("좀비")) {
                //    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data);
                (event.entity as Player).spawnParticle(Particle.BLOCK_CRACK, event.entity.location.x, event.entity.location.y + 1, event.entity.location.z, 5000, 1.5, 1.5, 1.5, Material.REDSTONE_BLOCK.createBlockData())
                (event.entity as Player).spawnParticle(Particle.BLOCK_CRACK, event.entity.location.x, event.entity.location.y + 2, event.entity.location.z, 5000, 1.5, 1.5, 1.5, Material.REDSTONE_BLOCK.createBlockData())

            }
        }
    }

}