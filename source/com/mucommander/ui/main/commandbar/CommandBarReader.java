/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.commandbar;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This class parses the XML file describing the command bar's buttons and associated actions.
 *
 * @author Maxence Bernard, Arik Hadas
 */
class CommandBarReader extends CommandBarIO {

    /** Temporarily used for XML parsing */
    private Vector actionsV;
    /** Temporarily used for XML parsing */
    private Vector alternateActionsV;
    /** Temporarily used for XML parsing */
    private KeyStroke modifier;
    /** Temporarily used for XML parsing */
    private String fileVersion;
    
    /** Parsed file */
    private AbstractFile file;

    /**
     * Starts parsing the XML description file.
     * 
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    CommandBarReader(AbstractFile file) throws SAXException, IOException, ParserConfigurationException {
    	this.file = file;
    	
    	InputStream in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(file), this);}
        finally {
            if(in!=null)
                try { in.close(); }
                catch(IOException e) {}
        }
    }

    ////////////////////
    ///// getters //////
    ////////////////////
    
    public String[] getActionsRead() {
    	int nbActions = actionsV.size();
    	String[] actionIds = new String[nbActions];
        actionsV.toArray(actionIds);
        return actionIds;
    }
    
    public String[] getAlternateActionsRead() {
    	int nbActions = alternateActionsV.size();
    	String[] alternateActionIds = new String[nbActions];
        alternateActionsV.toArray(alternateActionIds);
        return alternateActionIds;
    }
    
    public KeyStroke getModifierRead() {
    	return modifier;
    }
    
    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() {
        AppLogger.finest(file.getAbsolutePath()+" parsing started");

        actionsV = new Vector();
        alternateActionsV = new Vector();
        modifier = null;
    }

    public void endDocument() {
        AppLogger.finest(file.getAbsolutePath()+" parsing finished");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action class
            String actionAttribute = attributes.getValue(ACTION_ATTRIBUTE);
            // TODO: read action ids
            String actionId = ActionManager.extrapolateId(actionAttribute);
            if (actionId != null)
            	actionsV.add(actionId);
            else
            	AppLogger.warning("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action id for "+actionAttribute+" not found");

            // Resolve alternate action class (if any)
            actionAttribute = attributes.getValue(ALT_ACTION_ATTRIBUTE);
            if(actionAttribute==null)
                alternateActionsV.add(null);
            else
                if ((actionId = ActionManager.extrapolateId(actionAttribute)) != null)
                	alternateActionsV.add(actionId);
                AppLogger.warning("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action class "+actionAttribute+" not found");
        }
        else if(qName.equals(ROOT_ELEMENT)) {
        	// Retrieve modifier key (shift by default)
        	modifier = KeyStroke.getKeyStroke(attributes.getValue(MODIFIER_ATTRIBUTE));
            
        	// Note: early 0.8 beta3 nightly builds did not have version attribute, so the attribute may be null
            fileVersion = attributes.getValue(VERSION_ATTRIBUTE);
        }
    }
}
