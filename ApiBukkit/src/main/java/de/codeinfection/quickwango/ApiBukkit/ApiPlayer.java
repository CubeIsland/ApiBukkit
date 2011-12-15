package de.codeinfection.quickwango.ApiBukkit;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiPlayer implements Player
{
    private final String name;
    private String displayName;
    private String listName;
    private final Server server;

    public ApiPlayer(final String name, final Server server)
    {
        this.name = name;
        this.displayName = name;
        this.listName = name;
        this.server = server;
    }

    public String getName()
    {
        return this.name;
    }

    public Server getServer()
    {
        return this.server;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getPlayerListName()
    {
        return this.listName;
    }

    public void setPlayerListName(String listName)
    {
        this.listName = listName;
    }

    public Player getPlayer()
    {
        return this;
    }

    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("name", this.name);

        return result;
    }

    @Override
    public String toString()
    {
        return "ApiPlayer{" + "name=" + this.name + '}';
    }

    /**************************************************************************/

    public void setCompassTarget(Location lctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getCompassTarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InetSocketAddress getAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendRawMessage(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void kickPlayer(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void chat(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean performCommand(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSneaking() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSneaking(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSprinting() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSprinting(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSleepingIgnored(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSleepingIgnored() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playNote(Location lctn, byte b, byte b1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playNote(Location lctn, Instrument i, Note note) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playEffect(Location lctn, Effect effect, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendBlockChange(Location lctn, Material mtrl, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendBlockChange(Location lctn, int i, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendMap(MapView mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateInventory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void awardAchievement(Achievement a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, Material mtrl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, Material mtrl, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPlayerTime(long l, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getPlayerTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getPlayerTimeOffset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPlayerTimeRelative() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetPlayerTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getExperience() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExperience(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLevel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLevel(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getTotalExperience() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTotalExperience(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getExhaustion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExhaustion(float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSaturation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSaturation(float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFoodLevel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFoodLevel(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getBedSpawnLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PlayerInventory getInventory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ItemStack getItemInHand() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setItemInHand(ItemStack is) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSleeping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getSleepTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GameMode getGameMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setGameMode(GameMode gm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getHealth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHealth(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxHealth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getEyeHeight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getEyeHeight(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getEyeLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Block> getLineOfSight(HashSet<Byte> hs, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Block getTargetBlock(HashSet<Byte> hs, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Egg throwEgg() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Snowball throwSnowball() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Arrow shootArrow() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isInsideVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean leaveVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vehicle getVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getRemainingAir() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRemainingAir(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaximumAir() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaximumAir(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void damage(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void damage(int i, Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaximumNoDamageTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaximumNoDamageTicks(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLastDamage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLastDamage(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNoDamageTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNoDamageTicks(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVelocity(Vector vector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector getVelocity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public World getWorld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Location lctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Entity> getNearbyEntities(double d, double d1, double d2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getEntityId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFireTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxFireTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFireTicks(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDead() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Entity getPassenger() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setPassenger(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean eject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getFallDistance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFallDistance(float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLastDamageCause(EntityDamageEvent ede) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EntityDamageEvent getLastDamageCause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UUID getUniqueId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getTicksLived() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTicksLived(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOnline() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isBanned() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBanned(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWhitelisted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWhitelisted(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPermissionSet(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPermissionSet(Permission prmsn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPermission(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPermission(Permission prmsn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAttachment(PermissionAttachment pa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recalculatePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOp(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendMessage(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void giveExp(int amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getExp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExp(float exp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Player getKiller() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Location location, TeleportCause cause) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Entity destination, TeleportCause cause) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstPlayed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastPlayed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPlayedBefore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
