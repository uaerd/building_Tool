package example.build.tool;

// ดึงส่วนที่จำเป็นออกมาใช้งาน
import java.text.DecimalFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.World;
import org.bukkit.ChatColor;

public class building_tool extends JavaPlugin implements Listener {
    // ประกาศตัวแปรที่จำเป็น
    private static final String PREFIX = "[§d§lBuild §9§lTool§r]: ";
    private iron_building_tool Iron_Build_Tool;
    private diamond_building_tool Diamond_Build_Tool;
    private Location pos1;
    private Location pos2;
    private Block buildBlock;
    private Boolean unbuild = false;
    private Boolean buildBoolean = false;
    private Plugin plugin = this;
    private BukkitTask buildTask;
    @Override
    public void onEnable() {
        // สร้าง instance ของ iron_building_tool และ diamond_building_tool และส่ง plugin (this) เข้าไปใน constructor
        Iron_Build_Tool = new iron_building_tool(this);
        Diamond_Build_Tool = new diamond_building_tool(this);

        // เรียกเมท็อด CraftingRecipe() เพื่อเพิ่มสูตรการคราฟให้กับ Iron Building Tool และ Diamond Building Tool
        Iron_Build_Tool.CraftingRecipe();
        Diamond_Build_Tool.CraftingRecipe();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = event.getItem();

        // ตรวจสอบว่าผู้เล่นคลิกบล็อกด้วยไม้
        if (item != null && item.getType() == Material.STICK) {
            ItemMeta meta = item.getItemMeta();

            // ตรวจสอบว่าไอเท็มนี้มีค่าคงที่ 'Durability_building_tool' อยู่ใน PersistentDataContainer หรือไม่
            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(this, "Durability_building_tool"), PersistentDataType.INTEGER)) {
                Integer Durability = meta.getPersistentDataContainer().get(new NamespacedKey(this, "Durability_building_tool"), PersistentDataType.INTEGER);
                // ตรวจสอบการกด Shift 
                if (player.isSneaking()) {
                    player.sendTitle("§a§lDurability: "+ Durability, " ", 0, 0, 100);
                    // ตรวจสอบการกด คลิกบล็อก
                    if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        buildBlock = clickedBlock;
                        player.sendMessage(PREFIX + "§9The selected block will be used for placement: " + buildBlock.getType().toString());
                    }

                    if (buildBoolean) {
                        unbuild = true;
                    }
                    
                    return;
                } 

                if (buildBoolean || clickedBlock == null) {return;}
                // ตรวจสอบว่ามีการเลือก buildBlock หรือไม่
                if (buildBlock != null) {
                    if (pos1 == null) {
                        pos1 = clickedBlock.getLocation();
                        // แสดงข้อความให้ผู้เล่นทราบว่าตำแหน่ง pos1 ถูกตั้งไว้ที่
                        player.sendMessage(PREFIX + "§9set pos1 to: §c§l" + pos1.getX() + ", " + pos1.getY() + ", " + pos1.getZ());
                    } else  {
                        pos2 = clickedBlock.getLocation();
                        // แสดงข้อความให้ผู้เล่นทราบว่าตำแหน่ง pos2 ถูกตั้งไว้ที่
                        player.sendMessage(PREFIX + "§9set pos2 to: §c§l" + pos2.getX() + ", " + pos2.getY() + ", " + pos2.getZ());
                    }
                    getLogger().info("Current value of buildBoolean: " + buildBoolean.toString());
                    // หากมีทั้ง pos1 และ pos2 ถูกตั้งค่าแล้ว
                    if (pos1 != null && pos2 != null) {
                        // เรียกเมท็อด buildStructure เพื่อสร้างโครงสร้าง
                        buildBoolean = true;
                        buildStructure(player, Durability, item, pos1, pos2);
                        pos1 = null;
                        pos2 = null;
                    }
                } else {
                     // แสดงข้อความให้ผู้เล่นทราบให้เลือกบล็อกด้วยการกด Shift และคลิก
                    player.sendMessage(PREFIX + "§cPlease select a block By pressing the Sneak and click to block");
                }

            }
        }
    }
    private void buildStructure(Player player, int durability, ItemStack item, Location pos1, Location pos2) {
        World world = pos1.getWorld();
        Material buildBlockType = buildBlock.getType();
        ItemStack blockToRemoveItemStack = new ItemStack(buildBlockType, 1);
    
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        // คำนวณจำนวณบล็อกทั้งหมดในพื้นที่
        int totalBlocks = sizeX * sizeY * sizeZ;
        // ใช้ตัวแปรอ้างอิงอ็อบเจ็กต์เพื่อให้สามารถเปลี่ยนแปลงค่าได้
        final int[] currentPosition = {minX, minY, minZ, durability, 0}; // ใช้ array เก็บพิกัด x, y, z แทนตัวแปรเดี่ยว
        player.sendMessage(PREFIX + ChatColor.YELLOW + "build §c§l" + pos1.getX() + ", " + pos1.getY() + ", " + pos1.getZ() +" to " + pos1.getX() + ", " + pos1.getY() + ", " + pos1.getZ());
        player.sendMessage(PREFIX + ChatColor.YELLOW + "builder runing...");
	DecimalFormat df = new DecimalFormat("0.0");
        getLogger().info("Current value of buildBoolean: " + buildBoolean.toString());
        buildBoolean = true;
        buildTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // สร้าง Location ของบล็อกที่ต้องการวาง
            int currentX = currentPosition[0]; // ดึงค่า x จากอาร์เรย์
            int currentY = currentPosition[1]; // ดึงค่า y จากอาร์เรย์
            int currentZ = currentPosition[2]; // ดึงค่า z จากอาร์เรย์
            int DURABILITY = currentPosition[3]; // ดึงค่า คงทน จากอาเรย์
            int SetBlocks = currentPosition[4];
            Location blockLocation = new Location(world, currentX, currentY, currentZ);
            Block block = blockLocation.getBlock();
            if (unbuild) {
                player.sendMessage(PREFIX + "stop to build!");
                unbuild = false;
                buildBoolean = false;
                buildTask.cancel();                  
            }
            // ตรวจสอบว่าบล็อกว่างหรือไม่
            if (block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.LAVA || block.getType() == Material.TALL_GRASS || block.getType() == Material.TALL_SEAGRASS || block.getType() == Material.SHORT_GRASS || block.getType() == Material.SEAGRASS) {
                // ตรวจสอบว่าผู้เล่นมีบล็อกที่จะวางหรือไม่
                if (!player.getInventory().contains(buildBlockType)) {
                    player.sendMessage(PREFIX + ChatColor.RED + "You do not have the required block in your inventory.");
                    buildBoolean = false;
                    buildTask.cancel(); // ยกเลิกการทำงานหากไม่มีบล็อกที่จะวาง
                    return;
                }
    
                // วางบล็อกและลบไอเท็มที่ใช้จากสต็อกของผู้เล่น
                block.setType(buildBlockType);
                player.getInventory().removeItem(blockToRemoveItemStack);
    
                // ลดค่า durability ของโครงสร้าง
                DURABILITY--;
                if (DURABILITY <= 0) {
                    player.getInventory().removeItem(item);
                    player.sendMessage(PREFIX + ChatColor.RED +"Not enough durability to complete the structure.");
                    buildBoolean = false;
                    buildTask.cancel(); // ยกเลิกการทำงานหากความทนทานหมด
                    return;
                }
    
                // อัปเดต durability บนไอเท็ม
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Durability_building_tool"), PersistentDataType.INTEGER, DURABILITY);
                item.setItemMeta(meta);
            }
            SetBlocks++;
            // เพิ่มพิกัดให้กับตำแหน่งถัดไป (ขยับไปทางแกน x)
            currentX++;
            if (currentX > maxX) {
                currentX = minX; // ถ้าถึงขอบเขตของแกน x ให้กลับไปที่ต้นแถวของแกน x และขยับแกน y และ z
                currentY++;
                if (currentY > maxY) {
                    currentY = minY; // ถ้าถึงขอบเขตของแกน y ให้กลับไปที่ต้นแถวของแกน y และขยับแกน z
                    currentZ++;
                    if (currentZ > maxZ) {
                        player.sendMessage(PREFIX + "Structure has been created!");
            		    buildBoolean = false;
                        buildTask.cancel(); // ยกเลิกการทำงานเมื่อครบทุกตำแหน่ง
                    }
                }
            }

            // คำนวณ overallProgress ให้อยู่ในช่วง 0-100
            double overallProgress = ((double) SetBlocks / totalBlocks) * 100.0;
		String formattedProgress = df.format(overallProgress);
            // จำกัดค่าให้ overallProgress ไม่เกิน 100
            overallProgress = Math.min(overallProgress, 100.0);

            if (overallProgress >= 0 && overallProgress <= 25) {
                player.sendTitle(ChatColor.GREEN + "Progress: " + ChatColor.RED+formattedProgress+"%", ChatColor.LIGHT_PURPLE+"DURABILITY: "+ Integer.toString(DURABILITY), 0, 20, 10);
            } else if (overallProgress > 25 && overallProgress <= 75) {
                player.sendTitle(ChatColor.GREEN + "Progress: " + ChatColor.YELLOW+formattedProgress+"%", ChatColor.LIGHT_PURPLE+"DURABILITY: "+Integer.toString(DURABILITY), 0, 20, 10);
            } else if (overallProgress > 75 && overallProgress <= 100) {
                player.sendTitle(ChatColor.GREEN + "Progress: " + ChatColor.GREEN+formattedProgress+"%", ChatColor.LIGHT_PURPLE+"DURABILITY: "+Integer.toString(DURABILITY), 0, 20, 10);
            }

            currentPosition[0] = currentX;
            currentPosition[1] = currentY;
            currentPosition[2] = currentZ;
            currentPosition[3] = DURABILITY;
            currentPosition[4] = SetBlocks;


        }, 0L, 1L); // ให้วางบล็อก
    
        // ใช้ตัวแปร buildTask เพื่อระบุงานที่เรียกใช้งานเพื่อเมื่อเสร็จสิ้น
    }
    
    
}
