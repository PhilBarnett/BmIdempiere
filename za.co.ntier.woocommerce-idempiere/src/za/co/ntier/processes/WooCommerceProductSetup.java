package za.co.ntier.processes;

import org.compiere.model.I_M_AttributeSet;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeUse;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MPaySelection;
import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

import java.awt.RenderingHints.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.Woocommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;

import au.blindmot.model.MBLDProductPartType;
import za.co.ntier.model.MzzWoocommerceMap;
import za.co.ntier.model.MzzWoocommerceMapLine;
import za.co.ntier.model.X_zz_woocommerce;

/**
 *
 * Start a thread to collect unsynchronised orders from WooCommerce website
 *
 * @author Phil Barnett
 */

public class WooCommerceProductSetup extends SvrProcess {
	private static final String ATTRIBUTE = "10000003";
	private static final String PRODUCT_ADD = "10000004";
	private static final String PRODUCT_ATTRIBUTE = "10000005";
	private static final String PART_TYPE_CONTROL = "Tubular Blind Control";
	private static final String PART_TYPE_FABRIC = "Fabric";
	private static final String MULTI_SELECT_PARENT = "10000006";
	private static final String MULTI_SELECT_CHILD = "10000007";
	private String msg = "";
	private int noChildCounter = 0;

	//public class MyRunnable implements Runnable {

		@Override
		protected String doIt() throws Exception  {

			// Get WooCommerce defaults
			final PO wcDefaults;
			String whereClause = " isactive = 'Y' AND AD_Client_ID = ?";
			wcDefaults = new Query(getCtx(), X_zz_woocommerce.Table_Name, whereClause, null)
					.setParameters(new Object[] { Env.getAD_Client_ID(getCtx()) }).firstOnly();
			if (wcDefaults == null)
				throw new IllegalStateException("/nWooCommerce Defaults need to be set on iDempiere /n");

			// Setup client
			OAuthConfig config = new OAuthConfig((String) wcDefaults.get_Value("url"),
					(String) wcDefaults.get_Value("consumerkey"), (String) wcDefaults.get_Value("consumersecret"));
			Woocommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);

			// Get all with request parameters
			int wooCommID = getWoocommID(wcDefaults.get_ID());
		
			//Get the fields from Woocommerce for the current product
			Map<?, ?> wcProduct = wooCommerce.get(EndpointBaseType.PRODUCTS.getValue(), wooCommID);
			// Iterate through the fields
			@SuppressWarnings("unchecked")
			ArrayList<LinkedHashMap<Key, Object>> metaData = (ArrayList<LinkedHashMap<Key, Object>>) wcProduct.get("meta_data");
			ArrayList<LinkedHashMap<Key, Object>> fields = null;
			for(LinkedHashMap<Key, Object> metaD : metaData)
			{
				System.out.println("Field: " + " " + metaD.toString());
				if(metaD.containsValue("_wapf_fieldgroup"))
				{
					LinkedHashMap<Key, Object> values= (LinkedHashMap<Key, Object>) metaD.get("value");
					System.out.println("Field: " + " " + values.toString());
					fields = (ArrayList<LinkedHashMap<Key, Object>>) values.get("fields");
				}

			}
			//At this point, 'fields' contains all the product fields
			log.warning("fields retuned from Woocomerce: /n" + fields.toString());
			setupMappingfields(fields);
		
			StringBuilder message = new StringBuilder("Fields succesfully imported from WooCommerce for product: ");
			message.append(new MProduct(getCtx(), getParentProductID(), null).getName());
			message.append(". The process created ");
			message.append(noChildCounter);
			message.append(" \"NoChildRecords\" records. \"NoChildRecords\" ");
			message.append("are where there is a field in Woocommerce that does not map to anything, ");
			message.append("or the system cannot match the attributes or products. Please review these records and take action as required");
			msg = message.toString();//TODO: translate.
			addLog(getParentProductID(), null, null, msg, MProduct.Table_ID, getParentProductID());
			return "@OK@";
			
}

		/**
		 * Maps the values pulled from Woocommerce into Idempiere
		 * @param fields
		 */
		private void setupMappingfields(ArrayList<LinkedHashMap<Key, Object>> fields) {
			/*Fields arrive looking like this:
			 * 	{id=6471a396e490f, label=New field, description=null, type=section, required=false, class=null, width=null, parent_clone=[], options={choices=[], group=layout}, conditionals=[], clone={enabled=true, type=qty, label=Blind {n}}, pricing={type=fixed, amount=0, enabled=false}}
				{id=646a1c5084416, label=Width, description=This is the width the blind will be made at. For reveal fit applications, it's important that 4mm is DEDUCTED from the measured width., type=number, required=true, class=null, width=null, parent_clone={type=qty, label=Blind {n}}, options={choices=[], group=field, number_type=int}, conditionals=[], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				{id=6471a3f447c68, label=Location, description=Where in your home is the blind to go? For example, 'Bedroom 1 window 1' or 'Kitchen'., type=text, required=false, class=null, width=null, parent_clone={type=qty, label=Blind {n}}, options={group=field}, conditionals=[], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				{id=646a1c5c78eb3, label=Drop, description=null, type=number, required=true, class=null, width=null, parent_clone={type=qty, label=Blind {n}}, options={choices=[], group=field, number_type=int}, conditionals=[], clone={enabled=false}, pricing={type=fx, amount=lookuptable(rollerblinds;646a1c5084416;646a1c5c78eb3), enabled=true}}
				{id=6471a3adf6948, label=New field, description=null, type=sectionend, required=false, class=null, width=null, parent_clone=[], options={choices=[], group=layout}, conditionals=[], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				{id=645b9c76588df, label=Control options, description=null, type=select, required=false, class=null, width=null, parent_clone=[], options={choices=[{slug=6q9ox, label=Rollease chain drive, selected=false, options=[], pricing_type=fixed, pricing_amount=25}, {slug=ayy67, label=Durable chain drive, selected=false, options=[], pricing_type=fixed, pricing_amount=12}, {slug=i4fq9, label=Alpha motor, selected=false, options=[], pricing_type=fixed, pricing_amount=220}, {slug=9e1vu, label=Somfy motor, selected=false, options=[], pricing_type=fixed, pricing_amount=295}], group=field}, conditionals=[], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				{id=645ce0a543260, label=Fabric Options, description=null, type=image-swatch, required=false, class=null, width=null, parent_clone=[], options={choices=[{slug=b8edf, label=LeReve Blockout, selected=false, options=[], pricing_type=fx, pricing_amount=lookuptable(rollerfabricgrp8_50pcgmgst;646a1c5084416;646a1c5c78eb3), image=http://shop.blindmotion.com.au/wp-content/uploads/2023/05/1LEB300CON-150x150.jpg, attachment=32}, {slug=38034, label=Linesque Blockout, selected=false, options=[], pricing_type=fx, pricing_amount=lookuptable(rollerfabricgrp9_50pcgmgst;646a1c5084416;646a1c5c78eb3), image=http://shop.blindmotion.com.au/wp-content/uploads/2023/05/Linesque_Denim-150x150.jpg, attachment=34}, {slug=mc6k6, label=Vibe Blockout, selected=false, options=[], pricing_type=fx, pricing_amount=lookuptable(rollerfabricgrp2_50pcgmgst;646a1c5084416;646a1c5c78eb3), image=http://shop.blindmotion.com.au/wp-content/uploads/2023/05/Vibe_HERO-150x150.jpg, attachment=51}], group=field, label_pos=default, items_per_row=3, items_per_row_tablet=3, items_per_row_mobile=3, large_image=1}, conditionals=[], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				{id=645ce0d224cb8, label=LeReve, description=null, type=image-swatch, required=false, class=null, width=null, parent_clone=[], options={choices=[{slug=8hmgv, label=Concrete, selected=false, options=[], pricing_type=none, pricing_amount=0, image=http://shop.blindmotion.com.au/wp-content/uploads/2023/05/1LEB300CON-150x150.jpg, attachment=32}, {slug=49eyy, label=Onyx, selected=false, options=[], pricing_type=none, pricing_amount=0, image=http://shop.blindmotion.com.au/wp-content/uploads/2023/05/1LEB300ONY-150x150.jpg, attachment=33}], group=field, label_pos=default, items_per_row=3, items_per_row_tablet=3, items_per_row_mobile=3, large_image=1}, conditionals=[{rules=[{condition===, value=b8edf, field=645ce0a543260, generated=false}]}], clone={enabled=false}, pricing={type=fixed, amount=0, enabled=false}}
				
				The aim is to create ZZ_Woocommerce_Map and ZZ_Woocommerce_Map_Line records from the field data.
			 * 
			 */
			int mProductID = getParentProductID();
			MProduct mProduct = new MProduct(Env.getCtx(), mProductID, null);
			I_M_AttributeSet  IattributesSet = mProduct.getM_AttributeSet();
			MAttributeSet attributesSet = new MAttributeSet((MAttributeSet) IattributesSet);
			MAttribute attributes[] = attributesSet.getMAttributes(true);
			
		
			for(LinkedHashMap<Key, Object> fieldContent : fields)
			{
				String label = (String) fieldContent.get("label");
				boolean isAttribute = false;
				boolean newField = false;
				boolean isProductOption = false;
				if(getMatch(label, "New field" , 2))
				{
					newField = true;
					createNoChild(fieldContent, mProductID);
					//break;
				}
				else
				{
					
					//if(getMatch(label, ))
					//Is it an attribute?
				
					
					String[] labelParts = ((String) fieldContent.get("label")).split("\\s+");
					for(int i = 0; i < labelParts.length; i++)
					{
						if(isAttribute) break;
						
						
						for(int j = 0; j < attributes.length; j++)
						{
							if(getMatch(label, attributes[j].getName(), 1))//Try matching on the first word, EG "Width"
							{
								isAttribute = true;
								createAttribute(fieldContent,attributes[j],mProductID);
								break;
							}
							
							System.out.println(attributes[j].toString());
							if(labelParts[i].equalsIgnoreCase(attributes[j].getName()) ||
									fieldContent.get("label").equals(attributes[j].getName()))
							{
								isAttribute = true;
								createAttribute(fieldContent,attributes[j],mProductID);
								break;
							}
						}
					}
				}
				if(!isAttribute && !newField)
				{
					//Is it a product option? Need to check the field content against all the product options
					//for this parent product
					System.out.println(fieldContent.toString());
					String[] labelParts = ((String) fieldContent.get("label")).trim().split(" ");
					//Get all the parttypes for the parent
					MBLDProductPartType[] mBLDProductPartTypes = MBLDProductPartType.getMBLDProductPartTypes(getCtx(), mProductID, null);
					for(int q = 0; q < mBLDProductPartTypes.length; q++)//Iterate through the products on each parttset and look for a match
					{
						if(isProductOption) break;
					/*	if(((String) fieldContent.get("label")).equalsIgnoreCase(mBLDProductPartTypes[q].getName()))
							{
								isProductOption = true;
								break;
							}*/
						if( label.equalsIgnoreCase("Linesque LF colours") && q==3)
						{
							System.out.println(mBLDProductPartTypes[q].getName() + " q == " + q);
						}
						MProduct[] partSetProducts = MBLDProductPartType.getPartSetProducts(mProductID, mBLDProductPartTypes[q].getM_PartTypeID(), null);
						for(int k = 0; k < partSetProducts.length; k++)
						{
							//Need to match the Parttype name, not the product name?
							if(isProductOption) break;
							//String[] partTypeName = mBLDProductPartTypes[q].getName().trim().split(" ");
							
							if(((String) fieldContent.get("label")).equalsIgnoreCase(mBLDProductPartTypes[q].getName()))
							{
								isProductOption = true;
								createProductOption(fieldContent, mBLDProductPartTypes[q], partSetProducts);
								break;
							}
							
							System.out.println(mBLDProductPartTypes[q].getName());
									System.out.println("Partset product name: " + partSetProducts[k].getName().toString());
									System.out.println("PartTYpe " + mBLDProductPartTypes[q].getName());
									System.out.println("Field content label: " + fieldContent.get("label").toString());
									if(mBLDProductPartTypes[q].getName().equalsIgnoreCase("Fabric"))
										{
											System.out.println("Part name: " + mBLDProductPartTypes[q].getName());
											if(label.equalsIgnoreCase("Linesque LF colours"))
											{
												System.out.println("Linesque should match");
											}
										}
									//Try matching the name of the parType to WooComm field value
									if(getMatch(mBLDProductPartTypes[q].getName(), label, 3) || getMatch(mBLDProductPartTypes[q].getName(), label, 2))//EG Woocomm field label is "control options", PartType name 
									{
										isProductOption = true;
										createProductOption(fieldContent, mBLDProductPartTypes[q], partSetProducts);
										break;
									}
									//if(getMatch(mBLDProductPartTypes[q].getName(), label, 2))
									
									if(//(String) fieldContent.get("label")).equalsIgnoreCase(mBLDProductPartTypes[q].getName())||
											((String) fieldContent.get("label")).equalsIgnoreCase(partSetProducts[k].getName().replaceAll("( +)"," ").trim()) //Field label == Product name
											//Parttype name matches field label
											//|| labelParts[i].equalsIgnoreCase(mBLDProductPartTypes[q].getName().replaceAll("( +)"," ").trim()) //Field lable part == product name
											//|| labelParts[i].equalsIgnoreCase(partTypeName[z])//part of the label matches part of the partType name
											)
													 
									{
										isProductOption = true;
										createProductOption(fieldContent, mBLDProductPartTypes[q], partSetProducts);
										break;
									}
									else if(getProductMatch(partSetProducts, label, 3) > 0 )
										//|| (getProductMatch(partSetProducts, label, 2) > 0 
									/*||(getProductMatch(partSetProducts, label, 1)> 0 )*///))//Commented out because it gave too many incorrect matches
							//Match on product name
									{
										isProductOption = true;
										createProductOption(fieldContent, mBLDProductPartTypes[q], partSetProducts);
										break;
									}
									else if(getProductMatch(partSetProducts, label, 2) > 0 )
									{
										isProductOption = true;
										createProductOption(fieldContent, mBLDProductPartTypes[q], partSetProducts);
										break;
									}
									
								//}
							//}
							System.out.println(partSetProducts[k].toString());
			
						}//End for loop Partset Products.
						
					}//End for(int q = 0; q < mBLDProductPartTypes.length; q++) loop
					MBLDProductPartType[] mBLDProductPartTypes1 = MBLDProductPartType.getMBLDProductPartTypes(getCtx(), mProductID, null);
					for(int t = 0; t < mBLDProductPartTypes1.length; t++)
					{
						MProduct[] partSetProducts1 = MBLDProductPartType.getPartSetProducts(mProductID, mBLDProductPartTypes[t].getM_PartTypeID(), null);
							for(int k = 0; k < partSetProducts1.length; k++)
							if(!isProductOption)//Try and match on all individual words.
							{	
						
							for(int k1 = 0; k1 < partSetProducts1.length; k1++)
								{
										//Need to match the Parttype name, not the product name?
										if(isProductOption) break;
										String[] partTypeName = mBLDProductPartTypes1[t].getName().trim().split(" ");
									for(int z = 0; z < partTypeName.length; z++)
									{
										for(int i = 0; i < labelParts.length; i++) 
										{
											if(labelParts[i].equalsIgnoreCase(mBLDProductPartTypes1[t].getName().replaceAll("( +)"," ").trim()) //Field lable part == product name)
													|| labelParts[i].equalsIgnoreCase(partTypeName[z])) //part of the label matches part of the partType name)
											{
												isProductOption = true;
												createProductOption(fieldContent, mBLDProductPartTypes1[t], partSetProducts1);
												break;
											}
										}
									}
								}
					}
				}
					
				}
				//If it's neither an attribute or a product option, then set it as a 'Ignore No Child Records' ZZ_Woocommerce_Map
				if(!isAttribute && !newField && !isProductOption)
				{
					createNoChild(fieldContent, mProductID);
				}
			}
		}
		/**
		 * 
		 * @param fieldContent
		 * @param mProduct
		 * @param mBLDProductPartTypes
		 * @param partSetProducts
		 */
		private void createProductOption(LinkedHashMap<Key, Object> fieldContent, /*MProduct mProduct,*/ MBLDProductPartType mBLDProductPartTypes, MProduct[] partSetProducts) {
			//Single product add? Like a motor?
			if(!mBLDProductPartTypes.getName().equalsIgnoreCase(PART_TYPE_FABRIC))//If it's not fabric, try and create records
			{
				System.out.println(mBLDProductPartTypes.toString());
				//Add each Mapping as a single map with single map line
				LinkedHashMap<Key, Object> options = (LinkedHashMap<Key, Object>) fieldContent.get("options");
				ArrayList<LinkedHashMap<Key, Object>> choices = (ArrayList<LinkedHashMap<Key, Object>>) options.get("choices");
				//boolean mapCreated = false;
				for(LinkedHashMap<Key, Object> choice : choices)
				{
				    //Create 1 map record and one mapline record for each option
					if(mapAlreadyExists((String) fieldContent.get("id"), (String) choice.get("label")) < 0)
					{
						//Try and match the choice label to a product in the partset
						int mProductAddID = 0;
						boolean found = false;
						mProductAddID = getProductMatch(/*fieldContent, */partSetProducts, (String) choice.get("label"),3);
						if(mProductAddID == 0)mProductAddID = getProductMatch(/*fieldContent, */partSetProducts, (String) choice.get("label"),2);
						if(mProductAddID == 0)mProductAddID = getProductMatch(/*fieldContent, */partSetProducts, (String) choice.get("label"),1);
						if(mProductAddID > 0)//We have a Match
						{
							MzzWoocommerceMap map = createMap(fieldContent, getParentProductID());
							map.setwoocommerce_field_value((String) choice.get("label"));
							if(!map.save(get_TrxName()))
							{
								System.out.println(choice.get("label"));
							}
							
							MzzWoocommerceMapLine mapLine = createMapLine(map.get_ID());
							mapLine.setzz_woocommerce_map_type(PRODUCT_ADD);
							mapLine.setBLD_Product_PartType_ID(mBLDProductPartTypes.get_ID());//IS THIS CORRECT ID?
							mapLine.setM_Product_Line_ID(mProductAddID);//IS THIS THE CORRECT ID??
							mapLine.setDescription("ATTENTION: This record was automatically created by: " + this.getClass() +
									" you should check that the product mapped matches what should be there. You should also add brackets and non control mechs if required");
							//mapLine.setM_AttributeValue_ID(getAttributeValueID(attribute, choice.get("label")));
							mapLine.save(get_TrxName());
							log.warning("Creating map line -> " + PRODUCT_ADD + " with prouct: " + new MProduct(getCtx(), mProductAddID, null).getName());
							System.out.println(mapLine.toString());
							
						}
						else//We don't have a match, set no child
						{
							createNoChild(fieldContent, getParentProductID());
						}
					}
				}
		
				
			}
			else if(mBLDProductPartTypes.getName().equalsIgnoreCase(PART_TYPE_FABRIC))
			{
				boolean isMultiSelect = fieldContent.get("type").equals("multi-image-swatch");
				//Add the same product to each mapline, vary the colours
				if(mapParentAlreadyExists((String) fieldContent.get("id"), (String) fieldContent.get("label")) < 0)
				{
					if(isMultiSelect)
					{
						//Create Multi Select Parent
						MzzWoocommerceMap mapParent = createMap(fieldContent, getParentProductID());
						mapParent.setzz_woocommerce_m_select_type(MULTI_SELECT_PARENT);
						mapParent.setignore_no_child_records(true);
						mapParent.save(get_TrxName());
					}
					System.out.println(mBLDProductPartTypes.toString());
					//Add each Mapping as a single map with single map line
					LinkedHashMap<Key, Object> options = (LinkedHashMap<Key, Object>) fieldContent.get("options");
					ArrayList<LinkedHashMap<Key, Object>> choices = (ArrayList<LinkedHashMap<Key, Object>>) options.get("choices");
				
					boolean mapCreated = false;
					for(LinkedHashMap<Key, Object> choice : choices)
					{
						int fabricID = getProductMatch(/*fieldContent,*/ partSetProducts, (String) fieldContent.get("label"), 3);
						if(fabricID == 0)fabricID = getProductMatch(/*fieldContent, */partSetProducts, (String) fieldContent.get("label"),2);
						if(fabricID == 0)fabricID = getProductMatch(/*fieldContent, */partSetProducts, (String) fieldContent.get("label"),1);
						
						
						
						//mProductAddID = getProductMatch(/*fieldContent, */partSetProducts, (String) choice.get("label"),3);
						
						
						if(fabricID > 0 )//We have a fabric match with at least 1 colour
						{	
							int mAttributeProductID = getProductAttributeID(fabricID, "colour", "colours");
							MAttribute mAttribute = new MAttribute(getCtx(), mAttributeProductID, get_TrxName());
							if(getAttributeValueID(mAttribute, choice.get("label")) > 0)//The fabric has colours
	
							{
								if(mapAlreadyExists((String) fieldContent.get("id"), (String) choice.get("label")) < 0)
								{
									MzzWoocommerceMap map = createMap(fieldContent, getParentProductID());
									if(isMultiSelect)
									{
										map.setzz_woocommerce_m_select_type(MULTI_SELECT_CHILD);
									}
									map.setwoocommerce_field_value((String) choice.get("label"));
									map.save(get_TrxName());
									//Mapline 1
									MzzWoocommerceMapLine mapLine = createMapLine(map.get_ID());
									mapLine.setzz_woocommerce_map_type(PRODUCT_ADD);
									mapLine.setBLD_Product_PartType_ID(mBLDProductPartTypes.get_ID());//IS THIS CORRECT ID?
									mapLine.setM_Product_Line_ID(fabricID);
									mapLine.save(get_TrxName());
									log.warning("Creating map line -> " + PRODUCT_ADD + " with product: " + new MProduct(getCtx(), fabricID, null).getName());
									//Mapline 2
									MzzWoocommerceMapLine mapLine2 = new MzzWoocommerceMapLine(getCtx(),0, get_TrxName());
									PO.copyValues(mapLine, mapLine2);
									mapLine2.setzz_woocommerce_map_type(PRODUCT_ATTRIBUTE);
									//mapLine2.setBLD_Product_PartType_ID(mBLDProductPartTypes.get_ID());//IS THIS CORRECT ID?
									//mapLine2.setM_Product_Line_ID(fabricID);
									if(fabricID == 1001388)
									{
										System.out.println(new MProduct(getCtx(), fabricID, null).toString());
									}
									
									mapLine2.setM_Attribute_Product_ID(mAttributeProductID);//Hard coded to colour
								//	MAttribute mAttribute = new MAttribute(getCtx(), mAttributeProductID, get_TrxName());
									/*if(getAttributeValueID(mAttribute, choice.get("label")) < 1)//If there is attribute value then the record has no valid choices
									{
										mapCreated = true;
									}*/
								
									mapLine2.setM_Attributevalue_Product_ID(getAttributeValueID(mAttribute, choice.get("label")));
									log.warning("Creating map line -> " + PRODUCT_ATTRIBUTE + " with attribute ID: " + mAttributeProductID + " Label: " +choice.get("label"));
									mapLine2.save(get_TrxName());
									mapCreated = true;
							
							
							}
						}
					}
					}
					if(!mapCreated)
					{
						createNoChild(fieldContent, getParentProductID());
					}
					
				}//End If mapAlreadyExists
			}//End if
		}//createProductOption()

		/**
		 * 
		 * @param fabricID
		 * @param attributeNameSubString
		 * @return
		 */
		private int getProductAttributeID(int fabricID, String... attributeNameSubString) {
			int attributeID = 0;
			MProduct mProduct = new MProduct(getCtx(),fabricID, null);
			boolean found = false;
			MAttributeUse[] attributeUse = null;
			MAttributeSet attributeSet = mProduct.getAttributeSet();
			if (!(attributeSet == null))
			{
				attributeUse = attributeSet.getMAttributeUse();
			}
				
			for(int cycles = 0; cycles < attributeNameSubString.length; cycles++)
			{
				String[] attributeNameSplit = attributeNameSubString[cycles].trim().split(" ");
				for(int j =0; j < attributeUse.length; j++)
				{
					if(found) break;
					String strippedAttributeUse = attributeUse[j].getM_Attribute().getName().replaceAll("( +)"," ").trim();//Get rid of any extra white space in the attributename
					String[] attributeUseSplit = strippedAttributeUse.trim().split(" ");
					for(int y =0; y < attributeNameSplit.length; y++)
					{
						for(int g = 0; g < attributeUseSplit.length; g++)
						{
							if(attributeNameSplit[y].equalsIgnoreCase(attributeUseSplit[g]) ||
								attributeUse[j].getM_Attribute().getName().equalsIgnoreCase(attributeNameSubString[cycles]))
								{
									found = true;
									attributeID = attributeUse[j].getM_Attribute_ID();
									break;
								}
						}
						
					}
				}
		}
			return attributeID;
	}

		/**
		 * 
		 * @param fieldContent
		 * @param attribute
		 * @param mProductID
		 */
		private void createAttribute(LinkedHashMap<Key, Object> fieldContent, MAttribute attribute, int mProductID) {
			String attributeType = attribute.getAttributeValueType();
			System.out.println(attributeType.toString());
			System.out.println(attribute.toString());
			
			//If Number or string then create oneZZ_Woocommerce_Map and ZZ_Woocommerce_Map_Line record
			if(attributeType.equalsIgnoreCase("N") || attributeType.equalsIgnoreCase("S"))
			{
				if(mapAlreadyExistsAttribute((String) fieldContent.get("id"), (String) fieldContent.get("label")) < 0)
				{
					MzzWoocommerceMap mZZWoocommerceMap = createMap(fieldContent, mProductID);
					mZZWoocommerceMap.setAdd_To_Duplicate(true);
					mZZWoocommerceMap.save(get_TrxName());
					MzzWoocommerceMapLine mzzWoocommerceMapLine = createMapLine(mZZWoocommerceMap.get_ID());
					mzzWoocommerceMapLine.setzz_woocommerce_map_type(ATTRIBUTE);
					mzzWoocommerceMapLine.setM_Attribute_ID(attribute.get_ID());
					mzzWoocommerceMapLine.save(get_TrxName());
					System.out.println(mzzWoocommerceMapLine.toString());
				}
				
			}
			else if(attributeType.equalsIgnoreCase("L"))//If list loop through options and create ZZ_Woocommerce_Map_Line records
			{
				LinkedHashMap<Key, Object> options = (LinkedHashMap<Key, Object>) fieldContent.get("options");
				ArrayList<LinkedHashMap<Key, Object>> choices = (ArrayList<LinkedHashMap<Key, Object>>) options.get("choices");
			//	for(Map.Entry<Key, Object> entry : options.entrySet()) 
				for(LinkedHashMap<Key, Object> choice : choices)
				{
					if(mapAlreadyExists((String) fieldContent.get("id"), (String) choice.get("label")) < 0)
					{
						//Create 1 map record and one mapline record for each option
						MzzWoocommerceMap map = createMap(fieldContent, mProductID);
						map.setAdd_To_Duplicate(true);
						map.save(get_TrxName());
						map.setwoocommerce_field_value((String) choice.get("label"));
						map.save(get_TrxName());
						System.out.println(choice.get("label"));
						MzzWoocommerceMapLine mapLine = createMapLine(map.get_ID());
						mapLine.setzz_woocommerce_map_type(ATTRIBUTE);
						mapLine.setM_Attribute_ID(attribute.get_ID());
						mapLine.setM_AttributeValue_ID(getAttributeValueID(attribute, choice.get("label")));
						mapLine.save(get_TrxName());
						System.out.println(mapLine.toString());
						//Key key = entry.getKey();
					   // Object value = (Object) entry.getValue();
					}
				}
				
				System.out.println(attributeType.toString());
			}
			
		}

		/**
		 * 
		 * @param attribute
		 * @param attributeName
		 * @return
		 */
		private int getAttributeValueID(MAttribute attribute, Object attributeName) {
			MAttributeValue[] attributeValues = attribute.getMAttributeValues();
			int attributeValueID = 0;
			if(attributeValues == null)
			{
				return attributeValueID;
			}
			for(int v = 0; v < attributeValues.length; v++)
			{
				if(attributeValues[v].getName().equalsIgnoreCase((String) attributeName))
				{
					attributeValueID = attributeValues[v].get_ID();
					break;
				}
			}
			return attributeValueID;
		}
		/**
		 * 
		 * @param fieldContent
		 * @param mProductID
		 */
		private void createNoChild(LinkedHashMap<Key, Object> fieldContent, int mProductID) {
			if(mapAlreadyExistsAttribute((String)fieldContent.get("id"), (String)fieldContent.get("label")) < 0)
			{
				MzzWoocommerceMap mZZWoocommerceMap = createMap(fieldContent, mProductID);
				mZZWoocommerceMap.setDescription("Created by: " + this.getClass() + " . Please check that this record has been set correctly... If it should be setting a product or an attribute, please amend as appropriate.");
				//mZZWoocommerceMap.setwoocommerce_field_key((String) fieldContent.get("id"));
				//mZZWoocommerceMap.setwoocommerce_field_label((String) fieldContent.get("label"));
				mZZWoocommerceMap.setignore_no_child_records(true);
				//mZZWoocommerceMap.setM_Product_ID(mProductID);
				mZZWoocommerceMap.save(get_TrxName());
				System.out.println(mZZWoocommerceMap.toString());
				noChildCounter ++;
				
			}
		}
		
		/**
		 * 
		 * @param fieldContent
		 * @param mProductID
		 * @return
		 */
		private MzzWoocommerceMap createMap(LinkedHashMap<Key, Object> fieldContent, int mProductID) {
			MzzWoocommerceMap map = new MzzWoocommerceMap(getCtx(), 0 , get_TrxName());
			map.setwoocommerce_field_key((String) fieldContent.get("id"));
			map.setwoocommerce_field_label((String) fieldContent.get("label"));
			map.setDescription((String) fieldContent.get("description"));
			map.setM_Product_ID(mProductID);
			map.save(get_TrxName());
			System.out.println(map.toString());
			log.warning("Woocommerce map created with field key: " + (String) fieldContent.get("id") + ", field label: " + (String)fieldContent.get("label") + ", description: " + (String) fieldContent.get("description"));
			return map;
		}
		
		/**
		 * 
		 * @param mzzWoocommerceMapID
		 * @return
		 */
		private MzzWoocommerceMapLine createMapLine(int mzzWoocommerceMapID) {
			MzzWoocommerceMapLine mzzWoocommerceMapLine = new MzzWoocommerceMapLine(getCtx(), 0, get_TrxName());
			mzzWoocommerceMapLine.setZZ_Woocommerce_Map_ID(mzzWoocommerceMapID);
			mzzWoocommerceMapLine.save(get_TrxName());
			return mzzWoocommerceMapLine;
		}

		/**
		 * 
		 * @param wcDefaultsID
		 * @return
		 */
		private int getWoocommID(int wcDefaultsID) {
			//int mProductID = new MzzWoocommerceMap(getCtx(), getProcessInfo().getRecord_ID(), get_TrxName()).getM_Product_ID();
			int mProductID = getParentProductID();
			//int mProductID = Env.getContextAsInt(getCtx(), "2|0|M_Product_ID");
			StringBuffer sql = new StringBuffer("SELECT woocommerce_key FROM adempiere.zz_woocommerce_match zzm ");
			sql.append("WHERE zzm.zz_woocommerce_id = ");
			sql.append(wcDefaultsID);
			sql.append(" AND zzm.m_product_id = ");
			sql.append(mProductID);
			return DB.getSQLValue(null, sql.toString());
		}

		/**
		 * 
		 * @return
		 */
		private int getParentProductID() {
			return new MzzWoocommerceMap(getCtx(), getProcessInfo().getRecord_ID(), get_TrxName()).getM_Product_ID();
		}

	
	
	/**Attempts to match the names of the products in 'partSetProducts' with the 'label' parameter.
	 * First a straight match is tried, then string matching using less words is tried.
	 * 
	 * @param fieldContent
	 * @param partSetProducts
	 * @param label
	 * @return
	 */
	private int getProductMatch(/*LinkedHashMap<Key, Object> fieldContent, */MProduct[] partSetProducts, String label, int compareSize) {
		boolean found = false;
		//String strippedLabel = label.replaceAll("( +)"," ").trim();//Get rid of any extra white space
		//String[] labelSplit = strippedLabel.trim().toLowerCase().split(" ");
		
		if(label.equalsIgnoreCase("Fabric Options"))
		{
			System.out.println(label);
		}
		
		int mProductAddID = 0;
		for(int g = 0; g < partSetProducts.length; g++)//First, just try a straight match.
		{
			if(found) break;
			if(label.equalsIgnoreCase(partSetProducts[g].getName()))
			{
				found = true;
				mProductAddID = partSetProducts[g].get_ID();
				break;
			}
		}
		for(int d = 0; d < partSetProducts.length; d++)//If we don't get a straight match, lets see if we can match up the words in any order.
		{
			if(found) break;
			String productName = partSetProducts[d].getName();
			//String[] productNameSplit = productNameStrip.toLowerCase().trim().split(" ");
			//int labelLength = labelSplit.length;
			//int productNameLength = productNameSplit.length;
			//int compareSize= Math.min(productNameLength, labelLength);
			//Set<String> words1 = getFirstWords(productNameSplit, compareSize);
			//Set<String> words2 = getFirstWords(labelSplit,compareSize);
			if(getMatch(productName, label, compareSize))
			{
				found = true;
				mProductAddID = partSetProducts[d].get_ID();
				break;
			}
		} 
		return mProductAddID;
	}
	
	/**
	 * Compares 2 string parameters and matches on the first number of words in compareTarget. If compare target is
	 * larger than the smallest number of words in either string, the string with the least words is used as the number of words to be matched.
	 * @param string1
	 * @param string2
	 * @param compareTarget
	 * @return
	 */
	private boolean getMatch(String string1, String string2, int compareTarget) {
		String stripped1 = string1.replaceAll("( +)"," ").trim();//Get rid of any extra white space
		String stripped2 = string2.replaceAll("( +)"," ").trim();//Get rid of any extra white space
		
		int str1= stripped1.length();
		int str2 = stripped2.length();
		int compareSize= Math.min(str1, str2);
		if(compareTarget <= compareSize)
		{
			compareSize = compareTarget;
		}
		Set<String> words1 = getFirstWords(stripped1.trim().toLowerCase().split(" "), compareSize);
		Set<String> words2 = getFirstWords(stripped2.trim().toLowerCase().split(" "), compareSize);
		if(words1.equals(words2))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param fieldKey
	 * @param fieldValue
	 * 
	 */
	private int mapAlreadyExists(String fieldKey, String fieldValue) {
		String params[] = {fieldKey, fieldValue};
		StringBuffer sql = new StringBuffer("SELECT zzm.zz_woocommerce_map_id FROM zz_woocommerce_map zzm ");
		sql.append("WHERE zzm.woocommerce_field_key = ? ");
		sql.append("AND zzm.woocommerce_field_value = ? ");
		return DB.getSQLValue(null, sql.toString(), params);
	}
	/**
	 * 
	 * @param fieldKey
	 * @param fieldLabel
	 * @return
	 */
	private int mapAlreadyExistsAttribute(String fieldKey, String fieldLabel) {
		String params[] = {fieldKey, fieldLabel};
		StringBuffer sql = new StringBuffer("SELECT zzm.zz_woocommerce_map_id FROM zz_woocommerce_map zzm ");
		sql.append("WHERE zzm.woocommerce_field_key = ? ");
		sql.append("AND zzm.woocommerce_field_label = ? ");
		return DB.getSQLValue(null, sql.toString(), params);
	}
	
	/**
	 * 
	 * @param fieldKey
	 * @param fieldLabel
	 * @return
	 */
	private int mapParentAlreadyExists(String fieldKey, String fieldLabel) {
		String params[] = {fieldKey, fieldLabel, MULTI_SELECT_PARENT};
		StringBuffer sql = new StringBuffer("SELECT zzm.zz_woocommerce_map_id FROM zz_woocommerce_map zzm ");
		sql.append("WHERE zzm.woocommerce_field_key = ? ");
		sql.append("AND zzm.woocommerce_field_label = ? ");
		sql.append("AND zzm.zz_woocommerce_m_select_type = ?");
		return DB.getSQLValue(null, sql.toString(), params);
		}
	
	private Set<String> getFirstWords(String[] words, int length) {
		Set<String> wordSet = new HashSet<>(Arrays.asList(words).subList(0, Math.min(words.length, length)));
		return wordSet;
	}

	//}//MyRunnable
	@Override
	protected void prepare() {
	}

	/*@Override
	protected String doIt() throws Exception {
		Thread thread = new Thread(new MyRunnable());
		thread.start();

		return "Synchronisation of product from WooCommerce initiated";
	}*/
	
}//WooCommerceProductSetup
