package com.ethanicuss.merrymusic.musicplayer;

import com.ethanicuss.merrymusic.register.ModMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MusicPlayer {
    private static final Predicate<WitherBoss> WITHER_PREDICATE = (entity) -> {//Predicate for finding a specific wither boss. If you wanted, you could get real detailed here and only return Wither bosses with a certain hp value or something.
        return true;
    };
    private static final Predicate<Monster> ENEMY_PREDICATE = (entity) -> {//Same but for any hostile mob
        return true;
    };
    private static final Music[] bossMusic = {
            ModMusic.END_BOSS,
            ModMusic.WITHER,
            ModMusic.WITHER_PHASE2,
            ModMusic.WITHER_PHASE2,
            ModMusic.WITHER_SPAWN
    };
    private static boolean isPlayingBossMusic(MusicManager musicManager){
        for (Music music : bossMusic) {
            if (musicManager.isPlayingMusic(music)) {
                return true;
            }
        }
        return false;
    }
    public static Music findMusic(MusicManager musicManager){
        Music val = ModMusic.DAY;//default value, dw about this
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {//Check that we're actually in a game. If not, we're in the main menu.

            if (Objects.equals(player.level.dimension().location().getNamespace(), "minecraft:overworld")){//If the dimension is "minecraft:overworld"...
                if (player.level.getDayTime()%24000 < 12000) {//...and the current time is <12000...
                    val = ModMusic.OW_DAY;//...It's the daytime! So play some daytime music...
                }
                else{//...Otherwise...
                    val = ModMusic.OW_NIGHT;//...It's not daytime! So play some nighttime music.
                }
                if (player.getY() < 50){//Checks for player's Y level. If it's less than 50...
                    val = ModMusic.OW_CAVE;//...play some nice cave music.
                }
                if (player.getY() < 30 && player.level.getLightEmission(player.blockPosition()) == 0){//But if it's less than 30 and the player is in absolute darkness...
                    val = ModMusic.OW_SCARY;//...play some scary cave music.
                }//NOTE: these lower conditions will overwrite the val variable, so put the most generic conditions at the top.
            }

            if (!Objects.equals(player.level.dimension().location().getNamespace(), "minecraft:overworld") && !Objects.equals(player.level.dimension().location().getNamespace(), "minecraft:the_end")) {
                if (player.level.getDayTime()%24000 < 12000) {//If not in the overworld or the end, play some generic space day/night music.
                    val = ModMusic.DAY;
                }
                else{
                    val = ModMusic.NIGHT;
                }
            }

            if (Objects.equals(player.level.dimension().location().getNamespace(), "ad_astra:moon") && player.getY() < 95) {//If on the moon and lower than Y=95...
                val = ModMusic.MOON;
            }

            if (Objects.equals(player.level.dimension().location().getNamespace(), "minecraft:the_end")) {
                val = ModMusic.END;
                if (Minecraft.getInstance().gui.getBossOverlay().shouldPlayMusic()) {//If Minecraft believes we should be playing end boss music, then play our boss music instead
                    val = ModMusic.END_BOSS;
                }
            }

            if ((player.getHealth() < 12.0f || musicManager.isPlayingMusic(ModMusic.COMBAT)) && !isPlayingBossMusic(musicManager)) {//If the players health is less than 6 hearts or there is combat music playing AND boss music is not playing...
                double i = player.getX();//Do a bunch of stuff do find out if there are 3 or more enemies in line of sight of the player in a 20x20x20 box from the player.
                double j = player.getY();
                double k = player.getZ();
                float f = 20.0f;
                AABB box = new AABB((float) i - f, (float) j - f, (float) k - f, (float) (i + 1) + f, (float) (j + 1) + f, (float) (k + 1) + f);
                List<Monster> list = player.level.getEntities(EntityTypeTest.forClass(Monster.class), box, ENEMY_PREDICATE);
                int enemySeeCount = 0;
                int enemyCount = 0;
                boolean enemyDied = false;
                for (Monster e : list) {
                    enemyCount++;
                    if (e.hasLineOfSight(player)) {
                        enemySeeCount++;
                    }
                    if (e.isDeadOrDying()) {
                        enemyDied = true;
                        enemySeeCount--;
                        enemyCount--;
                    }
                }
                if (enemySeeCount >= 3) {//if there is 3 valid enemies, play combat music
                    val = ModMusic.COMBAT;
                }
                if (enemyDied && musicManager.isPlayingMusic(ModMusic.COMBAT)){//if an enemy just died, and the amount of valid enemies has turned to zero, end the combat music with a sound effect
                    if (enemySeeCount == 0){
                        val = ModMusic.COMBAT_END;
                    }
                }
                if (enemyCount == 0 && musicManager.isPlayingMusic(ModMusic.COMBAT)) {//if enemy count is zero, the player has escaped all nearby enemies, so end the combat music
                    val = ModMusic.COMBAT_END;
                }
            }

            {//BOSS MUSIC
                double i = player.getX();
                double j = player.getY();
                double k = player.getZ();
                float f = 30.0f;
                AABB box = new AABB((float) i - f, (float) j - f, (float) k - f, (float) (i + 1) + f, (float) (j + 1) + f, (float) (k + 1) + f);
                List<Monster> list = player.level.getEntities(EntityTypeTest.forClass(Monster.class), box, ENEMY_PREDICATE);
                int bossCount = 0;
                for (Monster e : list) {
                    switch (e.getName().toString().replace("translation{key='", "").replace("', args=[]}", "")) {//this is stupid but it works
                        case "entity.mutantmonsters.mutant_zombie" -> { //entity name as string, so it works with any mod - like "entity.<mod_id>.<entity>"
                            bossCount++;
                            if (e.hasLineOfSight(player) && !e.isDeadOrDying()) {//play boss music when boss is in sight, and the boss isn't currently dying
                                val = ModMusic.END_BOSS;
                            }
                            if (e.isDeadOrDying() && musicManager.isPlayingMusic(ModMusic.END_BOSS)) {//end boss music when it dies
                                val = ModMusic.COMBAT_END;
                            }
                        }
                    }
                }
                if (bossCount == 0 && isPlayingBossMusic(musicManager)) {//if there are no bosses but boss music is playing, play combat end sound
                    val = ModMusic.COMBAT_END;
                }
            }

            if (Minecraft.getInstance().gui.getBossOverlay().shouldDarkenScreen()) {//If the screen is darkened due to the Wither being present...
                double i = player.getX();//This is an example of a more complicated boss music implementation:
                double j = player.getY();
                double k = player.getZ();
                float f = 40.0f;
                AABB box = new AABB((float)i - f, (float)j - f, (float)k - f, (float)(i + 1) + f, (float)(j + 1) + f, (float)(k + 1) + f);
                List<WitherBoss> list = player.level.getEntities(EntityTypeTest.forClass(WitherBoss.class), box, WITHER_PREDICATE);
                boolean witherExists = false;
                boolean spawnExists = false;
                boolean phase2Exists = false;
                boolean witherDead = false;
                for (WitherBoss w : list) {
                    witherExists = true;
                    if (w.getInvulnerableTicks() > 0){
                        spawnExists = true;
                    }
                    else {
                        if (w.isPowered()) {
                            phase2Exists = true;
                        }
                    }
                    if (w.getHealth() <= 0){
                        witherDead = true;
                    }
                }
                if (spawnExists){//If there is a wither spawning, play the spawning music
                    val = ModMusic.WITHER_SPAWN;
                }
                else {
                    if (phase2Exists) {//If there is a wither in phase 2, play the phase 2 music
                        val = ModMusic.WITHER_PHASE2;
                    }
                    else{
                        if (witherExists) {//If there is a wither, play the wither music
                            val = ModMusic.WITHER;
                        }
                        else{
                            if (musicManager.isPlayingMusic(ModMusic.WITHER) || musicManager.isPlayingMusic(ModMusic.WITHER_PHASE2)){//If there is no wither, end any current wither boss music
                                val = ModMusic.COMBAT_END;
                            }
                        }
                    }
                }
                if (witherDead){//Wither dies - play death sound
                    val = ModMusic.WITHER_DEATH;
                }
            }

        } else {//MENU MUSIC
            val = ModMusic.NIGHT;
        }

        return val;
    }
}
