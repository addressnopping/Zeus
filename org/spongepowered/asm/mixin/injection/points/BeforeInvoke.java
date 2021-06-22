package org.spongepowered.asm.mixin.injection.points;

import java.util.Collection;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.InjectionPoint.AtCode;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.refmap.IMixinContext;

@AtCode("INVOKE")
public class BeforeInvoke extends InjectionPoint {
  protected final MemberInfo target;
  
  protected final boolean allowPermissive;
  
  protected final int ordinal;
  
  protected final String className;
  
  protected final IMixinContext context;
  
  public enum SearchType {
    STRICT, PERMISSIVE;
  }
  
  protected final Logger logger = LogManager.getLogger("mixin");
  
  private boolean log = false;
  
  public BeforeInvoke(InjectionPointData data) {
    super(data);
    this.target = data.getTarget();
    this.ordinal = data.getOrdinal();
    this.log = data.get("log", false);
    this.className = getClassName();
    this.context = data.getContext();
    this
      .allowPermissive = (this.context.getOption(MixinEnvironment.Option.REFMAP_REMAP) && this.context.getOption(MixinEnvironment.Option.REFMAP_REMAP_ALLOW_PERMISSIVE) && !this.context.getReferenceMapper().isDefault());
  }
  
  private String getClassName() {
    InjectionPoint.AtCode atCode = getClass().<InjectionPoint.AtCode>getAnnotation(InjectionPoint.AtCode.class);
    return String.format("@At(%s)", new Object[] { (atCode != null) ? atCode.value() : getClass().getSimpleName().toUpperCase() });
  }
  
  public BeforeInvoke setLogging(boolean logging) {
    this.log = logging;
    return this;
  }
  
  public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
    log("{} is searching for an injection point in method with descriptor {}", new Object[] { this.className, desc });
    if (!find(desc, insns, nodes, this.target, SearchType.STRICT) && this.target.desc != null && this.allowPermissive) {
      this.logger.warn("STRICT match for {} using \"{}\" in {} returned 0 results, attempting permissive search. To inhibit permissive search set mixin.env.allowPermissiveMatch=false", new Object[] { this.className, this.target, this.context });
      return find(desc, insns, nodes, this.target, SearchType.PERMISSIVE);
    } 
    return true;
  }
  
  protected boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes, MemberInfo member, SearchType searchType) {
    if (member == null)
      return false; 
    MemberInfo target = (searchType == SearchType.PERMISSIVE) ? member.transform(null) : member;
    int ordinal = 0;
    int found = 0;
    ListIterator<AbstractInsnNode> iter = insns.iterator();
    while (iter.hasNext()) {
      AbstractInsnNode insn = iter.next();
      if (matchesInsn(insn)) {
        MemberInfo nodeInfo = new MemberInfo(insn);
        log("{} is considering insn {}", new Object[] { this.className, nodeInfo });
        if (target.matches(nodeInfo.owner, nodeInfo.name, nodeInfo.desc)) {
          log("{} > found a matching insn, checking preconditions...", new Object[] { this.className });
          if (matchesInsn(nodeInfo, ordinal)) {
            log("{} > > > found a matching insn at ordinal {}", new Object[] { this.className, Integer.valueOf(ordinal) });
            if (addInsn(insns, nodes, insn))
              found++; 
            if (this.ordinal == ordinal)
              break; 
          } 
          ordinal++;
        } 
      } 
      inspectInsn(desc, insns, insn);
    } 
    if (searchType == SearchType.PERMISSIVE && found > 1)
      this.logger.warn("A permissive match for {} using \"{}\" in {} matched {} instructions, this may cause unexpected behaviour. To inhibit permissive search set mixin.env.allowPermissiveMatch=false", new Object[] { this.className, member, this.context, 
            Integer.valueOf(found) }); 
    return (found > 0);
  }
  
  protected boolean addInsn(InsnList insns, Collection<AbstractInsnNode> nodes, AbstractInsnNode insn) {
    nodes.add(insn);
    return true;
  }
  
  protected boolean matchesInsn(AbstractInsnNode insn) {
    return insn instanceof org.spongepowered.asm.lib.tree.MethodInsnNode;
  }
  
  protected void inspectInsn(String desc, InsnList insns, AbstractInsnNode insn) {}
  
  protected boolean matchesInsn(MemberInfo nodeInfo, int ordinal) {
    log("{} > > comparing target ordinal {} with current ordinal {}", new Object[] { this.className, Integer.valueOf(this.ordinal), Integer.valueOf(ordinal) });
    return (this.ordinal == -1 || this.ordinal == ordinal);
  }
  
  protected void log(String message, Object... params) {
    if (this.log)
      this.logger.info(message, params); 
  }
}
