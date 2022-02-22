package org.riskfirst.tweetprint.image.kite9;

import java.util.List;

import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Rectangle2D;
import org.kite9.diagram.model.style.Placement;
import org.w3c.dom.Element;

import kotlin.reflect.KClass;

public class SimpleElementContext implements ElementContext {

	@Override
	public void addChild(DiagramElement arg0, DiagramElement arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Rectangle2D bounds(Element arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Kite9ProcessingException contextualException(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Kite9ProcessingException contextualException(String arg0, Throwable arg1, Element arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String evaluateXPath(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> X getCSSStyleEnumProperty(String arg0, Element arg1, KClass<X> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Placement getCSSStylePlacementProperty(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntegerRange getCSSStyleRangeProperty(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DiagramElement> getChildDiagramElements(DiagramElement arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCssStyleDoubleProperty(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCssStyleStringProperty(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCssUnitSizeInPixels(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public XPathAware getDocumentReplacer(Element arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Placement getPlacement(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReference(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiagramElement getReferencedElement(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiagramElement getRegisteredDiagramElement(Element arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(Element arg0, DiagramElement arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double textWidth(String arg0, Element arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

}
