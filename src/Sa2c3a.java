import java.util.*;

import c3a.*;
import ts.*;
import sa.*;

public class Sa2c3a extends SaDepthFirstVisitor <C3aOperand> {
    private C3a c3a;
    int indentation;
    public C3a getC3a(){return this.c3a;}
    
    public Sa2c3a(SaNode root, Ts tableGlobale){
	c3a = new C3a();
	C3aTemp result = c3a.newTemp();
	C3aFunction fct = new C3aFunction(tableGlobale.getFct("main"));
	c3a.ajouteInst(new C3aInstCall(fct, result, ""));
	c3a.ajouteInst(new C3aInstStop(result, ""));
	indentation = 0;
	root.accept(this);
    }

    public void defaultIn(SaNode node)
    {
	for(int i = 0; i < indentation; i++){System.out.print(" ");}
	indentation++;
	System.out.println("<" + node.getClass().getSimpleName() + ">");
    }

	@Override
	public C3aOperand visit(SaDecTab node){
		defaultIn(node);
		C3aOperand result = new C3aVar(node.tsItem, new C3aConstant(node.getTaille()));
		defaultOut(node);
		return result;
	}

	//peut-etre SaExp à faire

	@Override
	public C3aOperand visit(SaExpInt node) {
		defaultIn(node);
		C3aOperand result = new C3aConstant (node.getVal());
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpVar node) {
		defaultIn(node);
		C3aOperand result = node.getVar().accept(this);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit (SaInstEcriture node) {
		defaultIn(node);
		c3a.ajouteInst(new C3aInstWrite(node.getArg().accept(this), ""));
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaInstTantQue node) {
		defaultIn(node);
		C3aLabel labelTest = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		c3a.addLabelToNextInst(labelTest);
		C3aOperand condition = node.getTest().accept(this);
		c3a.ajouteInst(new C3aInstJumpIfEqual(condition, c3a.False, labelContinue, ""));
		node.getFaire().accept(this);
		c3a.ajouteInst(new C3aInstJump(labelTest, ""));
		c3a.addLabelToNextInst(labelContinue);
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaDecFonc node) {
		defaultIn(node);
		c3a.ajouteInst(new C3aInstFBegin(node.tsItem, "entree fonction"));
		if (node.getParametres() != null) node.getParametres().accept(this);
		if (node.getVariable() != null) node.getVariable().accept(this);
		if (node.getCorps() != null) node.getCorps().accept(this);
		c3a.ajouteInst(new C3aInstFEnd(""));
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaInstAffect node){
		defaultIn(node);
		c3a.ajouteInst(new C3aInstAffect(node.getRhs().accept(this), node.getLhs().accept(this), ""));
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaVarSimple node) {
		defaultIn(node);
		C3aOperand result = new C3aVar(node.tsItem, null);
		defaultOut(node);
		return result;
	}

	public C3aOperand visit (SaAppel node) {
		defaultIn(node);
		C3aTemp result = c3a.newTemp();
		C3aFunction function = new C3aFunction(node.tsItem);
		if (node.getArguments() != null) {
			if (node.getArguments().getTete() != null) {
				c3a.ajouteInst(new C3aInstParam(node.getArguments().getTete().accept(this), ""));
			}
			if (node.getArguments().getQueue() != null) {
				SaLExp argumentQueue = node.getArguments().getQueue();
				c3a.ajouteInst(new C3aInstParam(argumentQueue.getTete().accept(this), ""));
				while ((argumentQueue = argumentQueue.getQueue()) != null) {
					c3a.ajouteInst(new C3aInstParam(argumentQueue.getTete().accept(this), ""));
				}
			}
		}
		c3a.ajouteInst(new C3aInstCall(function, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpAppel node) {
		defaultIn(node);
		C3aTemp result = c3a.newTemp();
		C3aFunction function = new C3aFunction(node.getVal().tsItem);
		if (node.getVal().getArguments() != null) {
			if (node.getVal().getArguments().getTete() != null) {
				c3a.ajouteInst(new C3aInstParam(node.getVal().getArguments().getTete().accept(this), ""));
			}
			if (node.getVal().getArguments().getQueue() != null) {
				SaLExp argumentQueue = node.getVal().getArguments().getQueue();
				c3a.ajouteInst(new C3aInstParam(argumentQueue.getTete().accept(this), ""));
				while ((argumentQueue = argumentQueue.getQueue()) != null) {
					c3a.ajouteInst(new C3aInstParam(argumentQueue.getTete().accept(this), ""));
				}
			}
		}
		c3a.ajouteInst(new C3aInstCall(function, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpAdd node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstAdd(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpSub node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstSub(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpMult node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstMult(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpDiv node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstDiv(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpInf node) {
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aTemp result = c3a.newTemp();
		C3aLabel labelTrue = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		c3a.ajouteInst(new C3aInstJumpIfLess(op1, op2, labelTrue,""));
		c3a.ajouteInst(new C3aInstAffect(c3a.False, result, ""));
		c3a.ajouteInst(new C3aInstJump(labelContinue, ""));
		c3a.addLabelToNextInst(labelTrue);
		c3a.ajouteInst(new C3aInstAffect(c3a.True, result, ""));
		c3a.addLabelToNextInst(labelContinue);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpEqual node) {
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aTemp result = c3a.newTemp();
		C3aLabel labelTrue = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		c3a.ajouteInst(new C3aInstJumpIfEqual(op1, op2, labelTrue,""));
		c3a.ajouteInst(new C3aInstAffect(c3a.False, result, ""));
		c3a.ajouteInst(new C3aInstJump(labelContinue, ""));
		c3a.addLabelToNextInst(labelTrue);
		c3a.ajouteInst(new C3aInstAffect(c3a.True, result, ""));
		c3a.addLabelToNextInst(labelContinue);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpAnd node) {
		defaultIn(node);
		C3aTemp result = c3a.newTemp();
		C3aLabel labelFalse = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		C3aOperand op1 = node.getOp1().accept(this);
		c3a.ajouteInst(new C3aInstJumpIfEqual(op1, c3a.False, labelFalse, ""));
		C3aOperand op2 = node.getOp2().accept(this);
		c3a.ajouteInst(new C3aInstJumpIfEqual(op2, c3a.False, labelFalse, ""));
		c3a.ajouteInst(new C3aInstAffect(c3a.True, result, ""));
		c3a.ajouteInst(new C3aInstJump(labelContinue, ""));
		c3a.addLabelToNextInst(labelFalse);
		c3a.ajouteInst(new C3aInstAffect(c3a.False, result, ""));
		c3a.addLabelToNextInst(labelContinue);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpOr node) {
		defaultIn(node);
		C3aTemp result = c3a.newTemp();
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aLabel labelContinue = c3a.newAutoLabel();
		C3aLabel labelTrue = c3a.newAutoLabel();
		c3a.ajouteInst(new C3aInstJumpIfNotEqual(op1, c3a.False, labelTrue, ""));
		c3a.ajouteInst(new C3aInstJumpIfNotEqual(op2, c3a.False, labelTrue, ""));
		c3a.ajouteInst(new C3aInstAffect(c3a.False, result, ""));
		c3a.ajouteInst(new C3aInstJump(labelContinue, ""));
		c3a.addLabelToNextInst(labelTrue);
		c3a.ajouteInst(new C3aInstAffect(c3a.True, result, ""));
		c3a.addLabelToNextInst(labelContinue);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpNot node) {
		C3aTemp result = c3a.newTemp();
		C3aOperand op1 = node.getOp1().accept(this);
		C3aLabel labelContinue = c3a.newAutoLabel();
		c3a.ajouteInst(new C3aInstAffect(c3a.True, result, ""));
		c3a.ajouteInst(new C3aInstJumpIfEqual(op1, c3a.False, labelContinue, null));
		c3a.ajouteInst(new C3aInstAffect(c3a.False, result, ""));
		c3a.addLabelToNextInst(labelContinue);
		return result;
	}

	@Override
	public C3aOperand visit(SaInstSi node) {
		defaultIn(node);
		C3aLabel labelFalse = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		C3aOperand labelTest = node.getTest().accept(this);
		if (node.getSinon() != null) {
			c3a.ajouteInst(new C3aInstJumpIfEqual(labelTest, c3a.False, labelFalse, null));
			node.getAlors().accept(this);
			c3a.ajouteInst(new C3aInstJump(labelContinue, ""));
			c3a.addLabelToNextInst(labelFalse);
			node.getSinon().accept(this);
			c3a.addLabelToNextInst(labelContinue);
		} else {
			c3a.ajouteInst(new C3aInstJumpIfEqual(labelTest, c3a.False, labelFalse, null));
			node.getAlors().accept(this);
			c3a.addLabelToNextInst(labelFalse);
		}
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaInstRetour node) {
		defaultIn(node);
		c3a.ajouteInst(new C3aInstReturn(node.getVal().accept(this), ""));
		c3a.ajouteInst(new C3aInstFEnd(""));
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaExpLire node) {
		defaultIn(node);
		C3aTemp result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstRead (result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaVarIndicee node) {
		defaultIn(node);
		C3aOperand result = new C3aVar(node.tsItem, node.getIndice().accept(this));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaInstIncremente node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getLhs().accept(this);
		C3aOperand op2 = node.getRhs().accept(this);
		C3aOperand temp = c3a.newTemp();
		c3a.ajouteInst(new C3aInstAdd(op1, op2, temp, ""));
		c3a.ajouteInst(new C3aInstAffect(temp, node.getLhs().accept(this), ""));
		defaultOut(node);
		return null;
	}

	@Override
	public C3aOperand visit(SaExpOptTer node)
	{
		defaultIn(node);
		C3aLabel labelFalse = c3a.newAutoLabel();
		C3aLabel labelContinue = c3a.newAutoLabel();
		C3aOperand temp = c3a.newTemp();
		C3aOperand labelTest = node.getTest().accept(this);
		c3a.ajouteInst(new C3aInstJumpIfEqual(labelTest, c3a.False, labelContinue, null));
		c3a.ajouteInst(new C3aInstAffect(node.getOui().accept(this), temp, ""));
		c3a.ajouteInst(new C3aInstJump(labelFalse, ""));
		c3a.addLabelToNextInst(labelContinue);
		c3a.ajouteInst(new C3aInstAffect(node.getNon().accept(this), temp, ""));
		c3a.addLabelToNextInst(labelFalse);
		defaultOut(node);
		return temp;
	}

	public void defaultOut(SaNode node)
    {
	indentation--;
		for(int i = 0; i < indentation; i++){System.out.print(" ");}
		System.out.println("</" + node.getClass().getSimpleName() + ">");
    }
    
    
}
