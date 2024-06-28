package example.build.tool;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class diamond_building_tool {
    private building_tool plugin;
    public diamond_building_tool(building_tool plugin) {
        this.plugin = plugin;
        // ทำสิ่งที่ต้องการทำเมื่อสร้าง iron_building_tool
    }

    public void CraftingRecipe() {
        // สร้าง ItemStack แท่งไม้ที่ต้องการให้เป็นตำแหน่งกลางของ ShapedRecipe
        ItemStack centerItem = new ItemStack(Material.STICK);
        centerItem.addUnsafeEnchantment(Enchantment.MENDING, 10); // เพิ่ม Enchantment MENDING ด้วยระดับ 10 ใน ItemStack
        ItemMeta meta = centerItem.getItemMeta();

        // เพิ่มข้อมูล NBT (ค่าคงทน) ใน ItemMeta
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Durability_building_tool"), PersistentDataType.INTEGER, 10000);

        // กำหนดชื่อสำหรับ ItemStack
        meta.setDisplayName("Diamond_Build_Tool");
        centerItem.setItemMeta(meta);

        // สร้าง ShapedRecipe โดยให้ตำแหน่งกลางเป็น ItemStack ของแท่งไม้ที่มี Enchantment
        ShapedRecipe customRecipe = new ShapedRecipe(new NamespacedKey(plugin, "Diamond_Build_Tool"), centerItem);

        // กำหนดรูปแบบการคราฟ (shape) โดยใช้อักษรแทนตำแหน่งบน crafting grid
        customRecipe.shape("*B*", "B%B", "*B*");

        // กำหนดวัสดุ (Ingredient) ในการคราฟ
        customRecipe.setIngredient('*', Material.DIAMOND_BLOCK);
        customRecipe.setIngredient('%', Material.STICK); 
        customRecipe.setIngredient('B', Material.DIAMOND);
        // เพิ่มสูตรคราฟเข้าใน Bukkit Server
        plugin.getServer().addRecipe(customRecipe);
    }
}