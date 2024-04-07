package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.common.recipe.ComplexMusicLabelRecipe;
import gg.moonflower.etched.core.Etched;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EtchedRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Etched.MOD_ID);

    public static final RegistryObject<SpecialRecipeSerializer<ComplexMusicLabelRecipe>> COMPLEX_MUSIC_LABEL = REGISTRY.register("complex_music_label", () -> new SimpleCraftingRecipeSerializer<>(ComplexMusicLabelRecipe::new));
}