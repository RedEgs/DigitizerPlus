package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.DigitizerPlus;

import java.util.ArrayList;
import java.util.HashMap;

public class DropdownGraphic extends Graphic {
    protected int x, y, u, v, sx, sy, dx, dy;

    protected boolean dropstate = false;
    protected boolean withinMenu = false;
    private long lastDropToggleTime = 0;
    private int debounceTime = 50;

    protected HashMap<String, Runnable> itemMap;
    protected HashMap<String, Runnable> NewItemMap;
    protected Integer selectedOption;

    protected ArrayList<ButtonGraphic> buttons = new ArrayList<>();
    protected ArrayList<String> buttonNames = new ArrayList<>();

    private ResourceLocation MAIN_TEX = new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");
    private Font font = Minecraft.getInstance().font;;
    private ImageGraphic baseGraphic;


    protected ButtonGraphic dropdownButtonDown = new ButtonGraphic(39, 3, MAIN_TEX, 169, 234, 4 ,5);
    protected ButtonGraphic dropdownButtonUp = new ButtonGraphic(39, 3, MAIN_TEX, 174, 234, 4 ,5);

    // dx, dy : Drawing Positions
    private ResourceLocation texture;

    public DropdownGraphic(int x, int y, ImageGraphic baseGraphic, HashMap itemMap) {
        super(x, y);
        this.x = x; this.y = y;
        this.baseGraphic = baseGraphic;

        this.itemMap = itemMap;

        this.dropdownButtonDown.setPosition(x+40, y+3);
        this.dropdownButtonDown.AddCallback(() -> {toggleDropstate(true);});

        this.dropdownButtonUp.setPosition(x+40, y+3);
        //this.dropdownButtonUp.AddCallback(() -> {toggleDropstate(false);});
        baseGraphic.setPosition(x, y);

    }

    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        dx = x+guiPositionX; dy = y+guiPositionY;
        int baseX = baseGraphic.getPosition().x; int baseY = baseGraphic.getPosition().y;

        this.baseGraphic.Draw(guiGraphics, mouseX, mouseY,deltaT, guiPositionX, guiPositionY, screen);

        if (selectedOption != null && dropstate == false) {
            guiGraphics.drawString(font, buttonNames.get(selectedOption), dx + 2, dy+1, 0xFFFFFF);
        }

        if (dropstate == false) {
            dropdownButtonDown.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
        } else {
            dropdownButtonUp.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
//            for (ButtonGraphic button: buttons) {
//                button.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY);
//            }

            withinMenu = withinMenu(dx, dy, dx+baseGraphic.sx, (11 * itemMap.size()), mouseX, mouseY);
            if (withinMenu) {
                RenderButtons(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
            }
////                guiGraphics.fill(dx, dy, dx+baseGraphic.sx, baseY+(11 * itemMap.size()), 0xFF11FF);
//                dropstate = false;
//            }
        }
    }

    @Override
    public void OnMouseDown(int mouseX, int mouseY, int button) {
        super.OnMouseDown(mouseX, mouseY, button);
        if (!dropstate) {
            dropdownButtonDown.OnMouseDown(mouseX, mouseY, button);
        } else {
            dropdownButtonUp.OnMouseDown(mouseX, mouseY, button);

            if (!withinMenu) {
                dropstate = false;
            }

            for (ButtonGraphic btn : buttons) {
                btn.OnMouseDown(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void OnMouseUp(int mouseX, int mouseY, int button) {
        super.OnMouseUp(mouseX, mouseY, button);
        if (!dropstate) {
            dropdownButtonDown.OnMouseUp(mouseX, mouseY, button);
        } else {
            dropdownButtonUp.OnMouseUp(mouseX, mouseY, button);
            for (ButtonGraphic btn : buttons) {
                btn.OnMouseUp(mouseX, mouseY, button);
            }
        }
    }

    private Runnable toggleDropstate(Boolean bool) {
        if (bool) {
            this.withinMenu = true;
        }
        this.dropstate = bool;
        this.lastDropToggleTime = System.currentTimeMillis(); // record drop open time
        return null;
    }
    private void RenderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        if (this.itemMap != this.NewItemMap) {
            this.buttons.clear();
            this.buttonNames.clear();

            int i = 0;
            for (Object actionName : itemMap.keySet()) {
                int drx = dropdownButtonDown.getPosition().x;
                int dry = dropdownButtonDown.getPosition().y;

                ButtonGraphic button = new ButtonGraphic(x, dry + (11 * i), MAIN_TEX, 74, 234, 47, 11);
                button.AddCallback(() -> {this.setDropdownItem( button );});

                button.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
                guiGraphics.drawString(font, (String) actionName, x + guiPositionX + 2, (x + guiPositionY + 2) + dry + (11 * i), 0xFFFFFF);

                buttons.add(button);
                buttonNames.add((String) actionName);
                i++;
            }
            this.NewItemMap = this.itemMap;
        }

        for (ButtonGraphic button: buttons) {
            int drx = dropdownButtonDown.getPosition().x;
            int dry = dropdownButtonDown.getPosition().y;
            String actionName = buttonNames.get(buttons.indexOf(button));

            button.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
            guiGraphics.drawString(font, (String) actionName, x + guiPositionX + 2, (x + guiPositionY + 2) + dry + (11 * buttons.indexOf(button)), 0xFFFFFF);

        }
    }

    public boolean withinMenu(int boxX, int boxY, int boxWidth, int boxHeight, int pointX, int pointY) {
        return pointX >= boxX && pointX <= boxX + boxWidth &&
                pointY >= boxY && pointY <= boxY + boxHeight;
    }

    private Runnable setDropdownItem(ButtonGraphic buttonGraphic) {
        if (System.currentTimeMillis() - lastDropToggleTime < debounceTime) return null;
        this.selectedOption = buttons.indexOf(buttonGraphic);
        this.dropstate = false;
        this.itemMap.get(buttonNames.get(this.selectedOption)).run();
        return null;
    }
}
