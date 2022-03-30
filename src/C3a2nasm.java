import java.util.*;

import nasm.*;
import ts.*;
import c3a.*;

public class C3a2nasm implements C3aVisitor <NasmOperand> {
    private C3a c3a;
    private Nasm nasm;
    private Ts tableGlobale;
    private TsItemFct currentFct;
    private NasmRegister esp;
    private NasmRegister ebp;

    
    public C3a2nasm(C3a c3a, Ts tableGlobale){
	this.c3a = c3a;
	nasm = new Nasm(tableGlobale);
	nasm.setTempCounter(c3a.getTempCounter());
	System.out.println("temp counter nb " + nasm.getTempCounter());
	
	this.tableGlobale = tableGlobale;
	this.currentFct = tableGlobale.getFct("main");
	esp = new NasmRegister(-1);
	esp.colorRegister(Nasm.REG_ESP);

	ebp = new NasmRegister(-1);
	ebp.colorRegister(Nasm.REG_EBP);

	NasmOperand res;
	for(C3aInst c3aInst : c3a.listeInst){
	    //	   	    System.out.println("<" + c3aInst.getClass().getSimpleName() + ">");
	    res = c3aInst.accept(this);
	}
	System.out.println("temp counter nb " + nasm.getTempCounter());
    }

    public Nasm getNasm(){return nasm;}

    /*--------------------------------------------------------------------------------------------------------------
      transforme une opérande trois adresses en une opérande asm selon les règles suivantes :
      
      C3aConstant -> NasmConstant
      C3aTemp     -> NasmRegister
      C3aLabel    -> NasmLabel
      C3aFunction -> NasmLabel
      C3aVar      -> NasmAddress
      --------------------------------------------------------------------------------------------------------------*/

    public NasmOperand visit(C3aConstant oper) {
        NasmOperand constant = new NasmConstant(oper.val);
	    return constant;
    }
    
    public NasmOperand visit(C3aLabel oper) {
        NasmOperand label = new NasmLabel(oper.toString());
	    return label;
    }
    
    public NasmOperand visit(C3aTemp oper) {
        NasmOperand register = new NasmRegister(oper.num);
	    return register;
    }
    
    public NasmOperand visit(C3aVar oper) {
        if (tableGlobale.getVar(oper.item.getIdentif()) != null) {
            NasmLabel label = new NasmLabel(oper.item.getIdentif());
            if (oper.index == null) return new NasmAddress(label);
            else {
                NasmRegister reg = nasm.newRegister();
                nasm.ajouteInst(new nasm.NasmMov(null, reg, oper.index.accept(this), ""));
                new NasmMul(null, reg, new NasmConstant(4), "");
                return new NasmAddress(new NasmLabel(oper.item.identif),'+',reg);
            }

        }
        else if (currentFct.getTable().getVar(oper.item.getIdentif()).isParam) {
            return new NasmAddress(ebp, '+', new NasmConstant( 8 + 4 * currentFct.getNbArgs() - oper.item.adresse));
        }
        else {
            return new NasmAddress(ebp, '-', new NasmConstant(oper.item.adresse + oper.item.getTaille()));
        }
    }
    
    public NasmOperand visit(C3aFunction oper) {
        NasmOperand label = new NasmLabel(oper.getValue().getIdentif());
        return label;
    }
    


    /*--------------------------------------------------------------------------------------------------------------*/


    public NasmOperand visit(C3aInstAdd inst) {
	    NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
	    nasm.ajouteInst(new NasmMov(label, inst.result.accept(this), inst.op1.accept(this), ""));
	    nasm.ajouteInst(new NasmAdd(null , inst.result.accept(this), inst.op2.accept(this), ""));
	    return null;
    }
    
    public NasmOperand visit(C3aInstSub inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        nasm.ajouteInst(new NasmMov(label, inst.result.accept(this), inst.op1.accept(this), ""));
        nasm.ajouteInst(new NasmSub(null , inst.result.accept(this), inst.op2.accept(this), ""));
	    return null;
    }

    public NasmOperand visit(C3aInstMult inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        nasm.ajouteInst(new NasmMov(label, inst.result.accept(this), inst.op1.accept(this), ""));
        nasm.ajouteInst(new NasmMul(null , inst.result.accept(this), inst.op2.accept(this), ""));
	    return null;
    }

    public NasmOperand visit(C3aInstDiv inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        NasmRegister register = nasm.newRegister();
        NasmRegister eax = nasm.newRegister();
        eax.colorRegister(Nasm.REG_EAX);
        nasm.ajouteInst(new NasmMov(label, eax, inst.op1.accept(this), ""));
        if(!(inst.op2.accept(this) instanceof NasmConstant)){
            nasm.ajouteInst(new NasmDiv(null, inst.op2.accept(this), ""));
        }
        else {
            nasm.ajouteInst(new NasmMov(null, register, inst.op2.accept(this), ""));
            nasm.ajouteInst(new NasmDiv(null, register, ""));
        }
        nasm.ajouteInst(new NasmMov(null, inst.result.accept(this), eax, ""));
        return null;
    }

    
    public NasmOperand visit(C3aInstCall inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        nasm.ajouteInst(new NasmSub(label, esp, new NasmConstant(4), ""));
        nasm.ajouteInst(new NasmCall(label, inst.op1.accept(this), ""));
        if(inst.result != null) nasm.ajouteInst(new NasmPop(label, inst.result.accept(this), ""));
        nasm.ajouteInst(new NasmAdd(label, esp, new NasmConstant(inst.op1.val.getNbArgs()*4), ""));
	    return null;
    }

    public NasmOperand visit(C3aInstFBegin inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        NasmOperand contant = new NasmConstant(4*currentFct.getTable().nbVar());
        currentFct = inst.val;
        nasm.ajouteInst(new NasmPush(new NasmLabel(inst.val.identif), ebp, ""));
        nasm.ajouteInst(new NasmMov(label, ebp, esp, ""));
        nasm.ajouteInst(new NasmSub(label, esp, contant, ""));
	    return null;
    }
    
    public NasmOperand visit(C3aInst inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfLess inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        if (inst.op1.accept(this) instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(null, register, inst.op1.accept(this), ""));
            nasm.ajouteInst(new NasmCmp(label, register, inst.op2.accept(this), ""));
        }
        else {
            nasm.ajouteInst(new NasmCmp(label, inst.op1.accept(this), inst.op2.accept(this), ""));

        }
        nasm.ajouteInst(new NasmJl(null, inst.result.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstRead inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        NasmRegister reg_eax = nasm.newRegister() ;
        reg_eax.colorRegister(Nasm.REG_EAX);
        nasm.ajouteInst(new NasmMov(label, reg_eax, new NasmLabel("sinput"), ""));
        nasm.ajouteInst(new NasmCall(null, new NasmLabel("readline"), ""));
        nasm.ajouteInst(new NasmCall(null, new NasmLabel("atoi"), ""));
        nasm.ajouteInst(new NasmMov(null, inst.result.accept(this), reg_eax, ""));
	    return null;
    }
    
    public NasmOperand visit(C3aInstAffect inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        nasm.ajouteInst(new NasmMov(label, inst.result.accept(this), inst.op1.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstFEnd inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        nasm.ajouteInst(new NasmAdd(label, esp, new NasmConstant(4*currentFct.getTable().nbVar()), ""));
        nasm.ajouteInst(new NasmPop(label, ebp, ""));
        nasm.ajouteInst(new NasmRet(label, ""));
	    return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfEqual inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        if (inst.op1.accept(this) instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(null, register, inst.op1.accept(this), ""));
            nasm.ajouteInst(new NasmCmp(label, register, inst.op2.accept(this), ""));
        }
        else {
            nasm.ajouteInst(new NasmCmp(label, inst.op1.accept(this), inst.op2.accept(this), ""));

        }
        nasm.ajouteInst(new NasmJe(null, inst.result.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfNotEqual inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        if (inst.op1.accept(this) instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(null, register, inst.op1.accept(this), ""));
            nasm.ajouteInst(new NasmCmp(label, register, inst.op2.accept(this), ""));
        }
        else {
            nasm.ajouteInst(new NasmCmp(label, inst.op1.accept(this), inst.op2.accept(this), ""));

        }
        nasm.ajouteInst(new NasmJne(null, inst.result.accept(this), ""));
        return null;
    }

    public NasmOperand visit(C3aInstJump inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        nasm.ajouteInst(new NasmJmp(label, inst.result.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstParam inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        nasm.ajouteInst(new NasmPush(label, inst.op1.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstReturn inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        nasm.ajouteInst(new NasmMov(label, new NasmAddress(ebp, '+', new NasmConstant(8)), inst.op1.accept(this), ""));
        return null;
    }
    
    public NasmOperand visit(C3aInstWrite inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        NasmRegister eax = nasm.newRegister();
        eax.colorRegister(Nasm.REG_EAX);
        nasm.ajouteInst(new NasmMov(label, eax, inst.op1.accept(this), ""));
        nasm.ajouteInst(new NasmCall(null, new NasmLabel("iprintLF"), ""));
	    return null;
    }

    public NasmOperand visit(C3aInstStop inst) {
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null ;
        NasmRegister ebx = nasm.newRegister();
        NasmRegister eax = nasm.newRegister();
        ebx.colorRegister(Nasm.REG_EBX);
        eax.colorRegister(Nasm.REG_EAX);
        nasm.ajouteInst(new NasmMov(label, ebx, new NasmConstant(0), ""));
        nasm.ajouteInst(new NasmMov(label, eax, new NasmConstant(1), ""));
        nasm.ajouteInst(new NasmInt(label, ""));
	    return null;
    }
    
}
