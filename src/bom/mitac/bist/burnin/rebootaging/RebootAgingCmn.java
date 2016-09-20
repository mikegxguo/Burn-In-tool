package bom.mitac.bist.burnin.rebootaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

public class RebootAgingCmn {

	public	static	int		GetSettingXmlData( SETTING_XML_DATA SettingXmlData, String FileName )
	{
		XmlPullParser	WorkXmlPullParser ;

		BufferedReader	WorkBufferedReader = null ;
		int				Ret = -1 ;
		boolean			LoopFlag = true ;

		File file = new File(FileName);
		if(!file.exists()) {
		    return Ret;
		}

		try {
			WorkBufferedReader = new BufferedReader( new FileReader( FileName ) ) ;
		} catch (FileNotFoundException e) {
			// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
			e.printStackTrace();
			Ret = -2 ;
		}

		if( WorkBufferedReader != null )
		{
			WorkXmlPullParser = Xml.newPullParser() ;

			try {
				WorkXmlPullParser.setInput( WorkBufferedReader ) ;
			} catch (XmlPullParserException e) {
				// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
				e.printStackTrace();
				LoopFlag = false ;
			}

			while( LoopFlag )
			{
				try {
					switch( WorkXmlPullParser.next() )
					{
						case XmlPullParser.START_TAG :
							if( WorkXmlPullParser.getName().equals( "Info" ) )
							{
								SettingXmlData.RebootTime						= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "RebootTime" ) ) ;
								SettingXmlData.BeginTime						= Long.valueOf( WorkXmlPullParser.getAttributeValue( null, "BeginTime" ) ) ;
								SettingXmlData.StartFlag						= WorkXmlPullParser.getAttributeValue( null, "StartFlag" ).equals( "0" ) ? false : true ;
								SettingXmlData.LogDateTime						= Long.valueOf( WorkXmlPullParser.getAttributeValue( null, "LogDateTime" ) ) ;
								SettingXmlData.OnPeriod							= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "OnPeriod" ) ) ;
								SettingXmlData.StartTime						= WorkXmlPullParser.getAttributeValue( null, "StartTime" ) ;
								SettingXmlData.StopTime							= WorkXmlPullParser.getAttributeValue( null, "StopTime" ) ;
								SettingXmlData.Count							= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "Count" ) ) ;
								SettingXmlData.FreeMemory_AfterBoot				= WorkXmlPullParser.getAttributeValue( null, "FreeMemory_AfterBoot" ).equals( "0" ) ? false : true ;
								SettingXmlData.FreeMemory_BeforeReboot			= WorkXmlPullParser.getAttributeValue( null, "FreeMemory_BeforeReboot" ).equals( "0" ) ? false : true ;
								SettingXmlData.WwanSignalStrength_AfterBoot		= WorkXmlPullParser.getAttributeValue( null, "WwanSignalStrength_AfterBoot" ).equals( "0" ) ? false : true ;
								SettingXmlData.WwanSignalStrength_BeforeReboot	= WorkXmlPullParser.getAttributeValue( null, "WwanSignalStrength_BeforeReboot" ).equals( "0" ) ? false : true ;
								SettingXmlData.WifiSignalStrength_AfterBoot		= WorkXmlPullParser.getAttributeValue( null, "WifiSignalStrength_AfterBoot" ).equals( "0" ) ? false : true ;
								SettingXmlData.WifiSignalStrength_BeforeReboot	= WorkXmlPullParser.getAttributeValue( null, "WifiSignalStrength_BeforeReboot" ).equals( "0" ) ? false : true ;
								SettingXmlData.Ping_Ip							= WorkXmlPullParser.getAttributeValue( null, "Ping_Ip" ) ;
								SettingXmlData.Ping_Timeout						= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "Ping_Timeout" ) ) ;
								SettingXmlData.Ping_AfterBoot					= WorkXmlPullParser.getAttributeValue( null, "Ping_AfterBoot" ).equals( "0" ) ? false : true ;
								SettingXmlData.Ping_BeforeReboot				= WorkXmlPullParser.getAttributeValue( null, "Ping_BeforeReboot" ).equals( "0" ) ? false : true ;
								SettingXmlData.BluetoothDevice_Timeout			= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "BluetoothDevice_Timeout" ) ) ;
								SettingXmlData.BluetoothDevice_AfterBoot		= WorkXmlPullParser.getAttributeValue( null, "BluetoothDevice_AfterBoot" ).equals( "0" ) ? false : true ;
								SettingXmlData.BluetoothDevice_BeforeReboot		= WorkXmlPullParser.getAttributeValue( null, "BluetoothDevice_BeforeReboot" ).equals( "0" ) ? false : true ;
								SettingXmlData.Output							= WorkXmlPullParser.getAttributeValue( null, "Output" ).replaceAll( "\\\\r\\\\n", "\r\n" ) ;
								SettingXmlData.Camera							= Integer.valueOf( WorkXmlPullParser.getAttributeValue( null, "Camera" ) ) ;
								Ret = 0 ;
							}
							break ;
						case XmlPullParser.END_DOCUMENT :
							LoopFlag = false ;
							break ;
						default :
							break ;
					}
				} catch (NumberFormatException e) {
					// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
					e.printStackTrace();
					LoopFlag = false ;
				} catch (XmlPullParserException e) {
					// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
					e.printStackTrace();
					LoopFlag = false ;
				} catch (IOException e) {
					// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
					e.printStackTrace();
					LoopFlag = false ;
				}
			}

			try {
				WorkBufferedReader.close() ;
			} catch (IOException e) {
				// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
				e.printStackTrace();
			}
		}

		return( Ret ) ;
	}


	public	static	int		SetSettingXmlData( SETTING_XML_DATA SettingXmlData, String FileName )
	{
		BufferedWriter	WorkBufferedWriter = null ;
		int				Ret = -1 ;


		try {
			WorkBufferedWriter = new BufferedWriter( new FileWriter( FileName ) ) ;
		} catch (IOException e) {
			// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
			e.printStackTrace();
			Ret = -2 ;
		}

		if( WorkBufferedWriter != null )
		{
			try {
				WorkBufferedWriter.write( "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" ) ;
				WorkBufferedWriter.write( "<Info\r\n" ) ;
				WorkBufferedWriter.write( String.format( "\tRebootTime=\"%d\"\r\n", SettingXmlData.RebootTime)) ;
				WorkBufferedWriter.write( String.format( "\tBeginTime=\"%d\"\r\n", SettingXmlData.BeginTime)) ;
				WorkBufferedWriter.write( String.format( "\tStartFlag=\"%d\"\r\n", SettingXmlData.StartFlag == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tLogDateTime=\"%d\"\r\n", SettingXmlData.LogDateTime ) ) ;
				WorkBufferedWriter.write( String.format( "\tOnPeriod=\"%d\"\r\n", SettingXmlData.OnPeriod ) ) ;
				WorkBufferedWriter.write( String.format( "\tCamera=\"%d\"\r\n", SettingXmlData.Camera ) ) ;
				WorkBufferedWriter.write( String.format( "\tStartTime=\"%s\"\r\n", SettingXmlData.StartTime ) ) ;
				WorkBufferedWriter.write( String.format( "\tStopTime=\"%s\"\r\n", SettingXmlData.StopTime ) ) ;
				WorkBufferedWriter.write( String.format( "\tCount=\"%d\"\r\n", SettingXmlData.Count ) ) ;
				WorkBufferedWriter.write( String.format( "\tFreeMemory_AfterBoot=\"%d\"\r\n", SettingXmlData.FreeMemory_AfterBoot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tFreeMemory_BeforeReboot=\"%d\"\r\n", SettingXmlData.FreeMemory_BeforeReboot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tWwanSignalStrength_AfterBoot=\"%d\"\r\n", SettingXmlData.WwanSignalStrength_AfterBoot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tWwanSignalStrength_BeforeReboot=\"%d\"\r\n", SettingXmlData.WwanSignalStrength_BeforeReboot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tWifiSignalStrength_AfterBoot=\"%d\"\r\n", SettingXmlData.WifiSignalStrength_AfterBoot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tWifiSignalStrength_BeforeReboot=\"%d\"\r\n", SettingXmlData.WifiSignalStrength_BeforeReboot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tPing_Ip=\"%s\"\r\n", SettingXmlData.Ping_Ip ) ) ;
				WorkBufferedWriter.write( String.format( "\tPing_Timeout=\"%d\"\r\n", SettingXmlData.Ping_Timeout ) ) ;
				WorkBufferedWriter.write( String.format( "\tPing_AfterBoot=\"%d\"\r\n", SettingXmlData.Ping_AfterBoot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tPing_BeforeReboot=\"%d\"\r\n", SettingXmlData.Ping_BeforeReboot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tBluetoothDevice_Timeout=\"%d\"\r\n", SettingXmlData.BluetoothDevice_Timeout ) ) ;
				WorkBufferedWriter.write( String.format( "\tBluetoothDevice_AfterBoot=\"%d\"\r\n", SettingXmlData.BluetoothDevice_AfterBoot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tBluetoothDevice_BeforeReboot=\"%d\"\r\n", SettingXmlData.BluetoothDevice_BeforeReboot == false ? 0 : 1 ) ) ;
				WorkBufferedWriter.write( String.format( "\tOutput=\"%s\">\r\n", SettingXmlData.Output.replaceAll( "\r\n", "\\\\r\\\\n" ) ) ) ;
				WorkBufferedWriter.write( "</Info>\r\n" ) ;
				Ret = 0 ;
			} catch (IOException e) {
				// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
				e.printStackTrace();
			}

			try {
				WorkBufferedWriter.close() ;
			} catch (IOException e) {
				// TODO 閾ｪ�?慕函謌舌＆繧後◆ catch �?悶Ο�?��?�?
				e.printStackTrace();
			}
		}

		if( Ret != 0 )
		{
			new File( FileName ).delete() ;
		}

		return( Ret ) ;
	}
}
