package gg.moonflower.etched.common.recipe;

import gg.moonflower.etched.common.item.ComplexMusicLabelItem;
import gg.moonflower.etched.common.item.MusicLabelItem;
import gg.moonflower.etched.common.item.SimpleMusicLabelItem;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedRecipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ComplexMusicLabelRecipe extends SpecialCraftingRecipe {

    public ComplexMusicLabelRecipe(Identifier resourceLocation, CraftingRecipeCategory category) {
        super(resourceLocation, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, World level) {
        int count = 0;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (!itemStack.isEmpty()) {
                if (!(itemStack.getItem() instanceof MusicLabelItem)) {
                    return false;
                }
                count++;
            }
        }

        return count == 2;
    }

    @Override
    public ItemStack assemble(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        List<ItemStack> labels = new ArrayList<>(2);
        for (int j = 0; j < container.size(); ++j) {
            ItemStack stack = container.getStack(j);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof MusicLabelItem) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    labels.add(copy);
                }
                if (labels.size() > 2) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (labels.size() != 2) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = this.getOutput(registryAccess);
        SimpleMusicLabelItem.setTitle(stack, SimpleMusicLabelItem.getTitle(labels.get(0)));
        SimpleMusicLabelItem.setAuthor(stack, SimpleMusicLabelItem.getAuthor(labels.get(0)));
        ComplexMusicLabelItem.setColor(stack, MusicLabelItem.getLabelColor(labels.get(0)), MusicLabelItem.getLabelColor(labels.get(1)));
        return stack;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryAccess) {
        return new ItemStack(EtchedItems.COMPLEX_MUSIC_LABEL.get());
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EtchedRecipes.COMPLEX_MUSIC_LABEL.get();
    }
}