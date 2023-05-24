package com.daniel.XMLConverter.ServerToXML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.daniel.XMLConverter.Converter;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.exceptions.ConvertionException;
import com.daniel.stcmessages.ChatHistoryMessage;
import com.daniel.stcmessages.STCMessage;

public class ChatHistoryConverter extends Converter {

    private String pathToTemplate = "src/main/XMLTemplates/chat/serverBroadcast.xml";

    @Override
    public String convertToSerializableXML(ArrayList<Object> params) throws ConvertionException {
        File xmlFile = new File(pathToTemplate);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConvertionException(e.getMessage());
        }

        Element root = document.getDocumentElement();
        for (Object o : params) {
            Element newDataElement = document.createElement("data");
            newDataElement.setTextContent((String) o);
            root.appendChild(newDataElement);
        }
        return serializeDocument(document);
    }

    @Override
    public CTSMessage convertFromSerializableXMLtoCM(Document serializedXML) {
        throw new UnsupportedOperationException("Unimplemented method 'convertFromSerializableXMLtoCM'");
    }

    @Override
    public STCMessage convertFromSerializableXMLtoSM(Document serializedXML) {
        ArrayList<String> list = new ArrayList<String>();
        NodeList children = serializedXML.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            String tmp = children.item(i).getTextContent();
            if (tmp.contains(":")) 
            list.add(tmp);
        }
        STCMessage ret = new ChatHistoryMessage(list);
        return ret;
    }
    
}
