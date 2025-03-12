package net.redegs.digitizerplus.api;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.EntityRenderer;
import net.redegs.digitizerplus.api.graphics.Graphic;
import net.redegs.digitizerplus.api.graphics.SlotGraphic;
import net.redegs.digitizerplus.network.ModNetwork;


import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleScreen extends Screen {
    protected static ResourceLocation TEXTURE;

    protected Integer imageWidth, imageHeight;
    protected Integer guiPositionX, guiPositionY;

    private boolean pauseScreen = false;
    protected HashMap<String, Integer> extraData = new HashMap<>();

    protected List<List<Slot>> inventories = new ArrayList<>();

    public boolean rendering = false;
    protected final List<Graphic> graphics = new ArrayList<>();
    protected final List<Graphic> graphicsToAdd = new ArrayList<>();


    public SimpleScreen(ResourceLocation texture, int i1, int i, Integer imageWidth, Integer imageHeight, String title) {
        super(Component.literal(title));
        if (texture != null) {
            this.TEXTURE = texture;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;

            this.guiPositionX = (width - this.imageWidth) / 2;
            this.guiPositionY = (height - this.imageHeight) / 2;

        }
    }

    // Setup funcs
    public SimpleScreen setPauseScreen(boolean shouldPause) {
        this.pauseScreen = shouldPause;
        return this;
    }

    // Widgets
    public SimpleScreen addEntityRenderer(int x, int y,  int scale, LivingEntity entity) {
        addGraphic(new EntityRenderer(x, y, scale, entity));
        return this;
    }

    public SimpleScreen addButton(int x, int y, ResourceLocation texture, int u, int v, int sx, int sy) {
        addGraphic(new ButtonGraphic(x, y, texture, u ,v, sx, sy));
        return this;
    }
    public SimpleScreen addButton(ButtonGraphic button) {
        addGraphic(button);
        return this;
    }

    public SimpleScreen addInventoryGrid(Container container, int sx, int sy, int xOffset, int yOffset) {
        List<Slot> inventory = new ArrayList<>(sx * sy);
        inventories.add(inventory);

        for (int i = 0; i < sx; ++i) {
            for (int l = 0; l < sy; ++l) {
                this.addSlot(new Slot(container, i+l, xOffset + l * 18, yOffset + i * 18), inventory);
            }
        }

        return this;
    }
    public SimpleScreen addInventorySlots(Container container, List<Vector2d> positions) {
        List<Slot> inventory = new ArrayList<>(container.getContainerSize());
        inventories.add(inventory);

        for (int i = 0; i < positions.size(); i++) {
            Vector2d vector = positions.get(i);
            Slot slot = new Slot(container, i, (int) vector.x, (int) vector.y);
            this.addSlot(slot, inventory);
        }
        return this;
    }

    public SimpleScreen addGraphic(Graphic graphic) {
        if (rendering) { graphicsToAdd.add(graphic); }
        else { graphics.add(graphic); }
        return this;
    }


    // Misc
    private Slot addSlot(Slot slot, List<Slot> inventory) {
        inventory.add(slot);
        graphics.add(new SlotGraphic(slot.x,  slot.y, slot));
        return slot;
    }

    public SimpleScreen addPlayerInventory(Player player, int xOffset, int yOffset) {
        List<Slot> inventory = new ArrayList<>(player.getInventory().getContainerSize());
        inventories.add(inventory);

        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(player.getInventory(), l + i * 9 + 9, xOffset + l * 18, yOffset + i * 18), inventory);
            }
        }
        return this;
    }
    public SimpleScreen addPlayerHotbar(Player player, int xOffset, int yOffset) {
        List<Slot> inventory = new ArrayList<>(player.getInventory().getContainerSize());
        inventories.add(inventory);

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(player.getInventory(), i, xOffset + i * 18, yOffset), inventory);
        }
        return this;
    }


    public void open() {
        Minecraft.getInstance().setScreen(this);
    }

//    public void openFromServer(Player player, Map<String, Integer> extraData) {
//        if (extraData == null) {
//            ModNetwork.sendToPlayer(new OpenScreenPacket(this, this.extraData), (ServerPlayer) player);
//        } else {
//            ModNetwork.sendToPlayer(new OpenScreenPacket(this, extraData), (ServerPlayer) player);
//        }
//    }



    @Override
    protected void init() { super.init(); }

    @Override
    public boolean isPauseScreen() {
        return pauseScreen;
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        renderBg(guiGraphics, mouseX, mouseY, delta);



        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    protected void renderBg(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend(); // Enable blending
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;
        this.guiPositionX = x; this.guiPositionY = y;


        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        //guiGraphics.blitNineSliced(TEXTURE, guiPositionX, guiPositionY, 100, 200, 4, 16, 16, 0, 0);

        renderGraphics(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public void renderGraphics(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        rendering = true;
        List<Graphic> snapshot = new ArrayList<>(graphics);
        for (Graphic graphic : snapshot) {
            graphic.Draw(guiGraphics, mouseX, mouseY, delta, guiPositionX, guiPositionY);
        }
        rendering = false;
        if (!graphicsToAdd.isEmpty()) {
            graphics.addAll(graphicsToAdd);
            graphicsToAdd.clear();
        }
    }




    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (Graphic graphic : graphics) {
            graphic.OnMouseDown((int) pMouseX, (int) pMouseY, pButton);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (Graphic graphic : graphics) {
            graphic.OnMouseUp((int) pMouseX, (int) pMouseY, pButton);
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

}