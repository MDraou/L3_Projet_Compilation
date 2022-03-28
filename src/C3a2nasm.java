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
	this.currentFct = null;
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
        NasmOperand label = new NasmLabel("l" + oper.getNumber());
	    return label;
    }
    
    public NasmOperand visit(C3aTemp oper) {
        NasmOperand register = new NasmRegister(oper.num);
	    return register;
    }
    
    public NasmOperand visit(C3aVar oper) { //pas fini du tout
        Set<String> listIdentifs = tableGlobale.variables.keySet();
        NasmOperand result = null;
        return result;

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
        NasmRegister ebx = nasm.newRegister();
        NasmRegister eax = nasm.newRegister();
        ebx.colorRegister(Nasm.REG_EBX);
        eax.colorRegister(Nasm.REG_EAX);
        if(!(inst.op2.accept(this) instanceof NasmConstant)){
            nasm.ajouteInst(new NasmMov(label, eax, inst.op1.accept(this), ""));
            nasm.ajouteInst(new NasmDiv(null, inst.op2.accept(this), ""));
        }
        else {
            nasm.ajouteInst(new NasmMov(label, eax, inst.op1.accept(this), ""));
            nasm.ajouteInst(new NasmMov(null, ebx, inst.op2.accept(this), ""));
            nasm.ajouteInst(new NasmDiv(null, ebx, ""));
        }
        return null;
    }

    
    public NasmOperand visit(C3aInstCall inst) {
	    return null;
    }

    public NasmOperand visit(C3aInstFBegin inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInst inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfLess inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstRead inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstAffect inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstFEnd inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfEqual inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstJumpIfNotEqual inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstJump inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstParam inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstReturn inst) {
	    return null;
    }
    
    public NasmOperand visit(C3aInstWrite inst) {
	    return null;
    }

    public NasmOperand visit(C3aInstStop inst) {
	    return null;
    }
    
}
