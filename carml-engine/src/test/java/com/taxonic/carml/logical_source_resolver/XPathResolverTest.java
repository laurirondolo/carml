package com.taxonic.carml.logical_source_resolver;

import static org.hamcrest.core.Is.is;
import static  org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.taxonic.carml.engine.EvaluateExpression;
import com.taxonic.carml.logical_source_resolver.LogicalSourceResolver.ExpressionEvaluatorFactory;
import java.util.List;
import net.sf.saxon.s9api.XdmItem;
import org.junit.Before;
import org.junit.Test;

public class XPathResolverTest {
	
	private static final String BOOK_ONE = 
			"<book category=\"cooking\">\r\n" + 
			"  <title lang=\"en\">Everyday Italian</title>\r\n" + 
			"  <author>Giada De Laurentiis</author>\r\n" + 
			"  <year>2005</year>\r\n" + 
			"  <price>30.00</price>\r\n" + 
			"</book>";
	
	private static final String SOURCE = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"\r\n" + 
			"<bookstore>\r\n" + 
			"\r\n" + 
			BOOK_ONE + 
			"\r\n" + 
			"<book category=\"children\">\r\n" + 
			"  <title lang=\"en\">Harry Potter</title>\r\n" + 
			"  <author>J K. Rowling</author>\r\n" + 
			"  <year>2005</year>\r\n" + 
			"  <price>29.99</price>\r\n" + 
			"</book>\r\n" +  
			"\r\n" + 
			"</bookstore>";
	
	private XPathResolver xpathResolver;
	private Iterable<XdmItem> nodeIterator;
	private List<XdmItem> nodes;
	
	@Before
	public void init() {
		xpathResolver = new XPathResolver();
		nodeIterator = xpathResolver.bindSource(SOURCE, "/bookstore/*").get();
		nodes = Lists.newArrayList(nodeIterator);
	}
	
	@Test
	public void sourceIterator_givenXmlSourceAndBookNodeSelector_shoulReturnAllNodes() {
		assertThat(nodes.size(), is(2));
		assertThat(nodes.get(0).toString(), is(equalToIgnoringWhiteSpace(BOOK_ONE)));
	}

	@Test
	public void expressionEvaluator_givenExpression_shoulReturnCorrectValue() {
		String expression = "//author/lower-case(.)";
		ExpressionEvaluatorFactory<XdmItem> evaluatorFactory = 
				xpathResolver.getExpressionEvaluatorFactory();
		EvaluateExpression evaluateExpression = evaluatorFactory.apply(nodes.get(0));
		assertThat(evaluateExpression.apply(expression).get(), is("giada de laurentiis"));
	}
	
	@Test
	public void expressionEvaluatorWithoutAutoTextExtraction_givenExpression_shoulReturnCorrectValue() {
		String expression = "//author";
		ExpressionEvaluatorFactory<XdmItem> evaluatorFactory = 
				xpathResolver.getExpressionEvaluatorFactory();
		EvaluateExpression evaluateExpression = evaluatorFactory.apply(nodes.get(0));
		assertThat(evaluateExpression.apply(expression).get(), is("Giada De Laurentiis"));
		
		// redefine XPath resolver to not auto-extract text
		boolean autoExtractNodeText = false;
		xpathResolver = new XPathResolver(autoExtractNodeText);
		
		nodeIterator = xpathResolver.bindSource(SOURCE, "/bookstore/*").get();
		evaluatorFactory = xpathResolver.getExpressionEvaluatorFactory();
		
		nodes = Lists.newArrayList(nodeIterator);
		evaluateExpression = evaluatorFactory.apply(nodes.get(0));
		assertThat(evaluateExpression.apply(expression).get(), is("<author>Giada De Laurentiis</author>"));
	}
}