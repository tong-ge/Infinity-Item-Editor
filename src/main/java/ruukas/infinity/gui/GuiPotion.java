package ruukas.infinity.gui;

import java.io.IOException;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ruukas.infinity.gui.action.GuiNumberField;
import ruukas.infinity.nbt.itemstack.tag.InfinityCustomPotionEffectList;
import ruukas.infinity.nbt.itemstack.tag.custompotioneffects.InfinityPotionEffectTag;

@SideOnly( Side.CLIENT )
public class GuiPotion extends GuiInfinity
{
    private GuiNumberField level;
    private GuiNumberField time;
    
    private int rotOff = 0;
    private int mouseDist = 0;
    private ItemStack potionIcon;
    
    public GuiPotion(GuiScreen lastScreen, ItemStack stack) {
        super( lastScreen, stack );
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        Keyboard.enableRepeatEvents( true );
        
        level = new GuiNumberField( 100, fontRenderer, 15, height - 33, 40, 18, 3 );
        level.minValue = 1;
        level.maxValue = 127;
        level.setValue( 1 );
        
        time = new GuiNumberField( 101, fontRenderer, 15, height - 60, 40, 18, 5 );
        time.minValue = 1;
        time.maxValue = 99999;
        time.setValue( 1 );
        
        potionIcon = new ItemStack( Items.POTIONITEM );
        
        PotionUtils.addPotionToItemStack( potionIcon, PotionType.REGISTRY.getObjectById( 0 ) );
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents( false );
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        level.updateCursorCounter();
        time.updateCursorCounter();
        if ( Math.abs( mouseDist - (height / 3) ) >= 16 )
            rotOff++;
    }
    
    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped( char typedChar, int keyCode ) throws IOException
    {
        if ( keyCode == 1 )
        {
            this.actionPerformed( this.backButton );
        }
        else
        {
            level.textboxKeyTyped( typedChar, keyCode );
            time.textboxKeyTyped( typedChar, keyCode );
        }
    }
    
    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked( int mouseX, int mouseY, int mouseButton ) throws IOException
    {
        super.mouseClicked( mouseX, mouseY, mouseButton );
        
        level.mouseClicked( mouseX, mouseY, mouseButton );
        time.mouseClicked( mouseX, mouseY, mouseButton );
        
        InfinityCustomPotionEffectList list = new InfinityCustomPotionEffectList( stack );
        InfinityPotionEffectTag[] activeEffects = list.getAll();
        int start = midY - 5 * activeEffects.length;
        if ( activeEffects.length > 0 && HelperGui.isMouseInRegion( mouseX, mouseY, 0, start, 5 + fontRenderer.getStringWidth( "Unbreaking 32767" ), 10 * activeEffects.length ) )
        {
            list.removePotionEffect( (mouseY - start) / 10 );
            return;
        }
        
        int r = height / 3;
        
        // mouseDist = (int) Math.sqrt(distX * distX + distY * distY);
        if ( Math.abs( mouseDist - r ) < 16 )
        {
            Set<ResourceLocation> keyset = Potion.REGISTRY.getKeys();
            double angle = (2 * Math.PI) / keyset.size();
            
            int lowDist = Integer.MAX_VALUE;
            Potion type = null;
            
            int i = 0;
            for ( ResourceLocation key : keyset )
            {
                double angleI = (((double) (rotOff) / 60d)) + (angle * i++);
                
                int x = (int) (midX + (r * Math.cos( angleI )));
                int y = (int) (midY + (r * Math.sin( angleI )));
                int distX = x - mouseX;
                int distY = y - mouseY;
                
                int dist = (int) Math.sqrt( distX * distX + distY * distY );
                
                if ( dist < 10 && dist < lowDist )
                {
                    lowDist = dist;
                    type = Potion.REGISTRY.getObject( key );
                }
            }
            
            if ( type != null )
            {
                new InfinityCustomPotionEffectList( stack ).set( new PotionEffect( type, time.getIntValue() * 20, level.getIntValue() - 1 ) );
            }
        }
        
        else if ( mouseX > midX - 15 && mouseX < midX + 15 && mouseY > midY - 15 && mouseY < midY + 15 )
        {
            Set<ResourceLocation> keyset = Potion.REGISTRY.getKeys();
            
            for ( ResourceLocation key : keyset )
            {
                new InfinityCustomPotionEffectList( stack ).set( new PotionEffect( Potion.REGISTRY.getObject( key ), time.getIntValue() * 20, level.getIntValue() - 1 ) );
            }
        }
    }
    
    @Override
    protected void actionPerformed( GuiButton button ) throws IOException
    {
        super.actionPerformed( button );
    }
    
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen( int mouseX, int mouseY, float partialTicks )
    {
        super.drawScreen( mouseX, mouseY, partialTicks );
        
        InfinityPotionEffectTag[] potionTags = new InfinityCustomPotionEffectList( stack ).getAll();
        for ( int i = 0 ; i < potionTags.length ; i++ )
        {
            InfinityPotionEffectTag e = potionTags[i];
            PotionEffect effect = e.getEffect();
            int ampli = effect.getAmplifier();
            drawString( fontRenderer, I18n.format( effect.getEffectName() ) + (ampli > 1 ? (" " + I18n.format( "potion.potency." + ampli ).trim()) : "") + " (" + (ampli + 1) + ")", 5, midY + i * 10 - potionTags.length * 5, HelperGui.MAIN_PURPLE );
        }
        
        level.drawTextBox();
        time.drawTextBox();
        
        drawString( fontRenderer, I18n.format( "gui.potion.time" ), 62, height - 56, HelperGui.MAIN_PURPLE );
        drawString( fontRenderer, I18n.format( "gui.potion.level" ), 62, height - 29, HelperGui.MAIN_PURPLE );
        
        int distX = midX - mouseX;
        int distY = midY - mouseY;
        mouseDist = (int) Math.sqrt( distX * distX + distY * distY );
        
        int r = height / 3;
        
        Set<ResourceLocation> keyset = Potion.REGISTRY.getKeys();
        double angle = (2 * Math.PI) / keyset.size();
        
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
        
        GlStateManager.scale( 5, 5, 1 );
        GlStateManager.translate( (width / 10), (height / 10), 0 );
        GlStateManager.rotate( rotOff * 3, 0.0f, 0.0f, -1.0f );
        this.itemRender.renderItemAndEffectIntoGUI( stack, -8, -8 );
        GlStateManager.rotate( rotOff * 3, 0.0f, 0.0f, 1.0f );
        GlStateManager.translate( -(width / 10), -(height / 10), 0 );
        
        GlStateManager.scale( 0.2, 0.2, 1 );
        
        int i = 0;
        for ( ResourceLocation key : keyset )
        {
            double angleI = (((double) (rotOff + (double) (Math.abs( mouseDist - r ) >= 16 ? partialTicks : 0d)) / 60d)) + (angle * i++);
            int x = (int) (midX + (r * Math.cos( angleI )));
            int y = (int) (midY + (r * Math.sin( angleI )));
            
            PotionEffect potEff = new PotionEffect( Potion.REGISTRY.getObject( key ), 20, level.getIntValue() - 1 );
            String displayString = I18n.format( potEff.getEffectName() );
            
            if ( potEff.getAmplifier() > 0 )
            {
                displayString += " " + I18n.format( "potion.potency." + potEff.getAmplifier() ).trim();
            }
            
            drawCenteredString( fontRenderer, TextFormatting.getTextWithoutFormattingCodes( displayString ), x, y - 17, HelperGui.MAIN_PURPLE );
            
            itemRender.renderItemAndEffectIntoGUI( potionIcon, x - 8, y - 8 );
            
            drawRect( x - 1, y - 1, x + 1, y + 1, HelperGui.getColorFromRGB( 255, 255, 255, 255 ) );
        }
        
        if ( mouseX > midX - 15 && mouseX < midX + 15 && mouseY > midY - 15 && mouseY < midY + 15 )
        {
            GlStateManager.translate( 0, 0, 300 );
            drawCenteredString( fontRenderer, I18n.format( "gui.enchanting.addall" ), midX, midY, HelperGui.MAIN_BLUE );
            GlStateManager.translate( 0, 0, -300 );
        }
        
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }
    
    @Override
    protected String getNameUnlocalized()
    {
        return "potion";
    }
}
