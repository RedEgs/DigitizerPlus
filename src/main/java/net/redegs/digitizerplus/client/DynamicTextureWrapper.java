package net.redegs.digitizerplus.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.DigitizerPlus;

import java.io.IOException;
import java.io.InputStream;

public class DynamicTextureWrapper {
    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private int width, height;

    public DynamicTextureWrapper(String tex_name, int width, int height) {
        this.width = width; this.height = height;
        dynamicTexture = new DynamicTexture(width, height, true);
        textureLocation = Minecraft.getInstance().getTextureManager().register(tex_name, dynamicTexture);
    }

    public void setImage(String path) {
        NativeImage image = null;
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(new ResourceLocation(DigitizerPlus.MOD_ID, path))) {
            image = NativeImage.read(stream);
            image.flipY();

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = image.getPixelRGBA(x, y);
                    dynamicTexture.getPixels().setPixelRGBA(x, y, color);
                }
            }

            dynamicTexture.upload();
            image.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) image.close();
        }
    }

    public void setPixel(int x, int y, int colorARGB) {
        dynamicTexture.getPixels().setPixelRGBA(x, y, colorARGB);
        dynamicTexture.upload();
    }

    public int getPixel(int x, int y) {
        return dynamicTexture.getPixels().getPixelRGBA(x, y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ResourceLocation getTexture() {
        return textureLocation;
    }

    public DynamicTexture getDynamicTexture() {
        return dynamicTexture;
    }

    public void close() {
        dynamicTexture.close(); // Frees GPU resource
    }

}
