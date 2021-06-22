package me.alpha432.oyvey.features.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.TestUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class HoleFiller extends Module {
  private static final BlockPos[] surroundOffset;
  
  private static HoleFiller INSTANCE = new HoleFiller();
  
  private final Setting<Integer> range;
  
  private final Setting<Integer> delay;
  
  private final Setting<Boolean> rotate;
  
  private final Setting<Integer> blocksPerTick;
  
  static {
    surroundOffset = BlockUtil.toBlockPos(EntityUtil.getOffsets(0, true));
  }
  
  private final Setting<Boolean> packet = register(new Setting("Packet", Boolean.valueOf(false)));
  
  private final Timer offTimer;
  
  private final Timer timer;
  
  private boolean isSneaking;
  
  private boolean hasOffhand = false;
  
  private final Map<BlockPos, Integer> retries;
  
  private final Timer retryTimer;
  
  private int blocksThisTick;
  
  private ArrayList<BlockPos> holes;
  
  private int trie;
  
  public HoleFiller() {
    super("HoleFill", "Fills holes around you.", Module.Category.COMBAT, true, true, true);
    this.range = register(new Setting("PlaceRange", Integer.valueOf(8), Integer.valueOf(0), Integer.valueOf(10)));
    this.delay = register(new Setting("Delay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(250)));
    this.blocksPerTick = register(new Setting("BlocksPerTick", Integer.valueOf(20), Integer.valueOf(8), Integer.valueOf(30)));
    this.rotate = register(new Setting("Rotate", Boolean.valueOf(true)));
    this.offTimer = new Timer();
    this.timer = new Timer();
    this.blocksThisTick = 0;
    this.retries = new HashMap<>();
    this.retryTimer = new Timer();
    this.holes = new ArrayList<>();
    setInstance();
  }
  
  public static HoleFiller getInstance() {
    if (INSTANCE == null)
      INSTANCE = new HoleFiller(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public void onEnable() {
    if (fullNullCheck())
      disable(); 
    this.offTimer.reset();
    this.trie = 0;
  }
  
  public void onTick() {
    if (isOn() && (((Integer)this.blocksPerTick.getValue()).intValue() != 1 || !((Boolean)this.rotate.getValue()).booleanValue()))
      doHoleFill(); 
  }
  
  public void onDisable() {
    this.retries.clear();
  }
  
  private void doHoleFill() {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial check : ()Z
    //   4: ifeq -> 8
    //   7: return
    //   8: aload_0
    //   9: new java/util/ArrayList
    //   12: dup
    //   13: invokespecial <init> : ()V
    //   16: putfield holes : Ljava/util/ArrayList;
    //   19: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   22: getfield player : Lnet/minecraft/client/entity/EntityPlayerSP;
    //   25: invokevirtual getPosition : ()Lnet/minecraft/util/math/BlockPos;
    //   28: aload_0
    //   29: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   32: invokevirtual getValue : ()Ljava/lang/Object;
    //   35: checkcast java/lang/Integer
    //   38: invokevirtual intValue : ()I
    //   41: ineg
    //   42: aload_0
    //   43: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   46: invokevirtual getValue : ()Ljava/lang/Object;
    //   49: checkcast java/lang/Integer
    //   52: invokevirtual intValue : ()I
    //   55: ineg
    //   56: aload_0
    //   57: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   60: invokevirtual getValue : ()Ljava/lang/Object;
    //   63: checkcast java/lang/Integer
    //   66: invokevirtual intValue : ()I
    //   69: ineg
    //   70: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   73: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   76: getfield player : Lnet/minecraft/client/entity/EntityPlayerSP;
    //   79: invokevirtual getPosition : ()Lnet/minecraft/util/math/BlockPos;
    //   82: aload_0
    //   83: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   86: invokevirtual getValue : ()Ljava/lang/Object;
    //   89: checkcast java/lang/Integer
    //   92: invokevirtual intValue : ()I
    //   95: aload_0
    //   96: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   99: invokevirtual getValue : ()Ljava/lang/Object;
    //   102: checkcast java/lang/Integer
    //   105: invokevirtual intValue : ()I
    //   108: aload_0
    //   109: getfield range : Lme/alpha432/oyvey/features/setting/Setting;
    //   112: invokevirtual getValue : ()Ljava/lang/Object;
    //   115: checkcast java/lang/Integer
    //   118: invokevirtual intValue : ()I
    //   121: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   124: invokestatic getAllInBox : (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;
    //   127: astore_1
    //   128: aload_1
    //   129: invokeinterface iterator : ()Ljava/util/Iterator;
    //   134: astore_2
    //   135: aload_2
    //   136: invokeinterface hasNext : ()Z
    //   141: ifeq -> 582
    //   144: aload_2
    //   145: invokeinterface next : ()Ljava/lang/Object;
    //   150: checkcast net/minecraft/util/math/BlockPos
    //   153: astore_3
    //   154: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   157: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   160: aload_3
    //   161: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   164: invokeinterface getMaterial : ()Lnet/minecraft/block/material/Material;
    //   169: invokevirtual blocksMovement : ()Z
    //   172: ifne -> 579
    //   175: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   178: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   181: aload_3
    //   182: iconst_0
    //   183: iconst_1
    //   184: iconst_0
    //   185: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   188: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   191: invokeinterface getMaterial : ()Lnet/minecraft/block/material/Material;
    //   196: invokevirtual blocksMovement : ()Z
    //   199: ifne -> 579
    //   202: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   205: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   208: aload_3
    //   209: iconst_1
    //   210: iconst_0
    //   211: iconst_0
    //   212: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   215: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   218: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   223: getstatic net/minecraft/init/Blocks.BEDROCK : Lnet/minecraft/block/Block;
    //   226: if_acmpne -> 233
    //   229: iconst_1
    //   230: goto -> 234
    //   233: iconst_0
    //   234: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   237: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   240: aload_3
    //   241: iconst_1
    //   242: iconst_0
    //   243: iconst_0
    //   244: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   247: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   250: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   255: getstatic net/minecraft/init/Blocks.OBSIDIAN : Lnet/minecraft/block/Block;
    //   258: if_acmpne -> 265
    //   261: iconst_1
    //   262: goto -> 266
    //   265: iconst_0
    //   266: ior
    //   267: ifeq -> 559
    //   270: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   273: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   276: aload_3
    //   277: iconst_0
    //   278: iconst_0
    //   279: iconst_1
    //   280: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   283: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   286: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   291: getstatic net/minecraft/init/Blocks.BEDROCK : Lnet/minecraft/block/Block;
    //   294: if_acmpne -> 301
    //   297: iconst_1
    //   298: goto -> 302
    //   301: iconst_0
    //   302: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   305: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   308: aload_3
    //   309: iconst_0
    //   310: iconst_0
    //   311: iconst_1
    //   312: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   315: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   318: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   323: getstatic net/minecraft/init/Blocks.OBSIDIAN : Lnet/minecraft/block/Block;
    //   326: if_acmpne -> 333
    //   329: iconst_1
    //   330: goto -> 334
    //   333: iconst_0
    //   334: ior
    //   335: ifeq -> 559
    //   338: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   341: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   344: aload_3
    //   345: iconst_m1
    //   346: iconst_0
    //   347: iconst_0
    //   348: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   351: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   354: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   359: getstatic net/minecraft/init/Blocks.BEDROCK : Lnet/minecraft/block/Block;
    //   362: if_acmpne -> 369
    //   365: iconst_1
    //   366: goto -> 370
    //   369: iconst_0
    //   370: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   373: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   376: aload_3
    //   377: iconst_m1
    //   378: iconst_0
    //   379: iconst_0
    //   380: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   383: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   386: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   391: getstatic net/minecraft/init/Blocks.OBSIDIAN : Lnet/minecraft/block/Block;
    //   394: if_acmpne -> 401
    //   397: iconst_1
    //   398: goto -> 402
    //   401: iconst_0
    //   402: ior
    //   403: ifeq -> 559
    //   406: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   409: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   412: aload_3
    //   413: iconst_0
    //   414: iconst_0
    //   415: iconst_m1
    //   416: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   419: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   422: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   427: getstatic net/minecraft/init/Blocks.BEDROCK : Lnet/minecraft/block/Block;
    //   430: if_acmpne -> 437
    //   433: iconst_1
    //   434: goto -> 438
    //   437: iconst_0
    //   438: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   441: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   444: aload_3
    //   445: iconst_0
    //   446: iconst_0
    //   447: iconst_m1
    //   448: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   451: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   454: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   459: getstatic net/minecraft/init/Blocks.OBSIDIAN : Lnet/minecraft/block/Block;
    //   462: if_acmpne -> 469
    //   465: iconst_1
    //   466: goto -> 470
    //   469: iconst_0
    //   470: ior
    //   471: ifeq -> 559
    //   474: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   477: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   480: aload_3
    //   481: iconst_0
    //   482: iconst_0
    //   483: iconst_0
    //   484: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   487: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   490: invokeinterface getMaterial : ()Lnet/minecraft/block/material/Material;
    //   495: getstatic net/minecraft/block/material/Material.AIR : Lnet/minecraft/block/material/Material;
    //   498: if_acmpne -> 559
    //   501: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   504: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   507: aload_3
    //   508: iconst_0
    //   509: iconst_1
    //   510: iconst_0
    //   511: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   514: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   517: invokeinterface getMaterial : ()Lnet/minecraft/block/material/Material;
    //   522: getstatic net/minecraft/block/material/Material.AIR : Lnet/minecraft/block/material/Material;
    //   525: if_acmpne -> 559
    //   528: getstatic me/alpha432/oyvey/features/modules/combat/HoleFiller.mc : Lnet/minecraft/client/Minecraft;
    //   531: getfield world : Lnet/minecraft/client/multiplayer/WorldClient;
    //   534: aload_3
    //   535: iconst_0
    //   536: iconst_2
    //   537: iconst_0
    //   538: invokevirtual add : (III)Lnet/minecraft/util/math/BlockPos;
    //   541: invokevirtual getBlockState : (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   544: invokeinterface getMaterial : ()Lnet/minecraft/block/material/Material;
    //   549: getstatic net/minecraft/block/material/Material.AIR : Lnet/minecraft/block/material/Material;
    //   552: if_acmpne -> 559
    //   555: iconst_1
    //   556: goto -> 560
    //   559: iconst_0
    //   560: istore #4
    //   562: iload #4
    //   564: ifne -> 570
    //   567: goto -> 135
    //   570: aload_0
    //   571: getfield holes : Ljava/util/ArrayList;
    //   574: aload_3
    //   575: invokevirtual add : (Ljava/lang/Object;)Z
    //   578: pop
    //   579: goto -> 135
    //   582: aload_0
    //   583: getfield holes : Ljava/util/ArrayList;
    //   586: aload_0
    //   587: <illegal opcode> accept : (Lme/alpha432/oyvey/features/modules/combat/HoleFiller;)Ljava/util/function/Consumer;
    //   592: invokevirtual forEach : (Ljava/util/function/Consumer;)V
    //   595: aload_0
    //   596: invokevirtual toggle : ()V
    //   599: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #92	-> 0
    //   #93	-> 7
    //   #95	-> 8
    //   #96	-> 19
    //   #97	-> 128
    //   #98	-> 154
    //   #99	-> 202
    //   #100	-> 562
    //   #101	-> 567
    //   #103	-> 570
    //   #105	-> 579
    //   #106	-> 582
    //   #107	-> 595
    //   #108	-> 599
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   562	17	4	solidNeighbours	Z
    //   154	425	3	pos	Lnet/minecraft/util/math/BlockPos;
    //   0	600	0	this	Lme/alpha432/oyvey/features/modules/combat/HoleFiller;
    //   128	472	1	blocks	Ljava/lang/Iterable;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   128	472	1	blocks	Ljava/lang/Iterable<Lnet/minecraft/util/math/BlockPos;>;
  }
  
  private void placeBlock(BlockPos pos) {
    for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
      if (entity instanceof net.minecraft.entity.EntityLivingBase)
        return; 
    } 
    if (this.blocksThisTick < ((Integer)this.blocksPerTick.getValue()).intValue()) {
      int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
      int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
      if (obbySlot == -1 && eChestSot == -1)
        toggle(); 
      boolean smartRotate = (((Integer)this.blocksPerTick.getValue()).intValue() == 1 && ((Boolean)this.rotate.getValue()).booleanValue());
      if (smartRotate) {
        this.isSneaking = BlockUtil.placeBlockSmartRotate(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, true, ((Boolean)this.packet.getValue()).booleanValue(), this.isSneaking);
      } else {
        this.isSneaking = BlockUtil.placeBlock(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, ((Boolean)this.rotate.getValue()).booleanValue(), ((Boolean)this.packet.getValue()).booleanValue(), this.isSneaking);
      } 
      int originalSlot = mc.player.inventory.currentItem;
      mc.player.inventory.currentItem = (obbySlot == -1) ? eChestSot : obbySlot;
      mc.playerController.updateController();
      TestUtil.placeBlock(pos);
      if (mc.player.inventory.currentItem != originalSlot) {
        mc.player.inventory.currentItem = originalSlot;
        mc.playerController.updateController();
      } 
      this.timer.reset();
      this.blocksThisTick++;
    } 
  }
  
  private boolean check() {
    if (fullNullCheck()) {
      disable();
      return true;
    } 
    this.blocksThisTick = 0;
    if (this.retryTimer.passedMs(2000L)) {
      this.retries.clear();
      this.retryTimer.reset();
    } 
    return !this.timer.passedMs(((Integer)this.delay.getValue()).intValue());
  }
}
