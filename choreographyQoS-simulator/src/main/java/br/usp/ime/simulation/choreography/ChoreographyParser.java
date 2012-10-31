package br.usp.ime.simulation.choreography;

import java.io.File;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import br.usp.ime.simulation.choreography.model.ChoreographyModel;
import br.usp.ime.simulation.choreography.model.ChoreographyModel.MessageInteractionType;
import br.usp.ime.simulation.choreography.model.Service;
import br.usp.ime.simulation.choreography.model.ServiceOperation;

public class ChoreographyParser {
	
	private String choreographyModelFile="";
	
	public ChoreographyParser(String file){
		this.choreographyModelFile=file;
	}
	
	public void generateChoreographyModel(){
        SAXBuilder builder = new SAXBuilder();
        Document xml = null;
        try {
                xml = builder.build(new File(this.choreographyModelFile));
        } catch (JDOMException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
      
        //getting root element from XML document
        Element root = xml.getRootElement();
      
        System.out.println("Root element of XML document is : " + root.getName());
        System.out.println("Number of nodes in this XML : " + root.getChildren().size());
        
        Element services=root.getChild("services");
        Element interactions=root.getChild("interactions");
        System.out.println("# services:" +services.getChildren().size());
        //Handling services
        for(Element service: services.getChildren() ){
        	Service s= new Service(service.getAttributeValue("name"));
        	for(Element serviceOperation:service.getChildren()){
        		ServiceOperation so = new ServiceOperation(s, serviceOperation.getAttributeValue("name"));
        		System.out.println("SO: "+so);
        		s.addServiceOperation(so);
        	}
        			
        	ChoreographyModel.addService(s);
        }
        
        System.out.println("Choreography Model have "+ChoreographyModel.getRoleServices().size());
        
        //handling interactions, only sequence
        for(Element interactionElement:interactions.getChildren()){
        		Element source = interactionElement.getChild("source");
        		Element target = interactionElement.getChild("target");
        		
        		ServiceOperation so1= ChoreographyModel.findServiceOperation(source.getAttributeValue("service"), source.getAttributeValue("operation"));
        		System.out.println("SO1: <"+source.getAttributeValue("service")+","+source.getAttributeValue("operation")+">");
        		ServiceOperation so2= ChoreographyModel.findServiceOperation(target.getAttributeValue("service"), target.getAttributeValue("operation"));
        		System.out.println("SO2: "+so2);
        		String interactionType=target.getAttributeValue("type");
        		MessageInteractionType miType;
        		
        		if(interactionType.equals("REQUEST_RESPONSE"))
        			miType=MessageInteractionType.Request_Response;
        		else if (interactionType.equals("REQUEST"))
        			miType=MessageInteractionType.Request;
        		else
        			miType=null;
        		so1.addDependencies(so2, miType);
        }

	}
	
	 public static void main(String args[]){
		 ChoreographyParser parser = new ChoreographyParser("smallChoreographySpecification.xml");
		 parser.generateChoreographyModel();
		 
	 }
         
}
