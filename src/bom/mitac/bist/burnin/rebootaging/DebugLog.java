package bom.mitac.bist.burnin.rebootaging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import android.util.Log;

public class DebugLog {

	private	String			m_FileName = null ;
	private	Calendar		m_Calendar = null ;


	public	DebugLog( String FileName )
	{
		m_FileName = FileName ;
	}


	public	String	Output( boolean DateTimeFlag, String FormatString, Object... FormatArgument )
	{
		BufferedWriter	WorkBufferedWriter = null ;
		String			DebugString = new String() ;


		if( DateTimeFlag )
		{
			Calendar		WorkCalendar = Calendar.getInstance() ;
			if( m_Calendar == null
				|| WorkCalendar.get( Calendar.YEAR ) != m_Calendar.get( Calendar.YEAR )
				|| WorkCalendar.get( Calendar.MONTH ) != m_Calendar.get( Calendar.MONTH )
				|| WorkCalendar.get( Calendar.DAY_OF_MONTH ) != m_Calendar.get( Calendar.DAY_OF_MONTH ) )
			{
				m_Calendar = WorkCalendar ;
				DebugString += String.format( "-------------------- %04d/%02d/%02d --------------------\r\n",
												WorkCalendar.get( Calendar.YEAR ), WorkCalendar.get( Calendar.MONTH ) + 1, WorkCalendar.get( Calendar.DAY_OF_MONTH ) ) ;
			}
			DebugString += String.format( "[%02d:%02d:%02d.%03d]",
											WorkCalendar.get( Calendar.HOUR_OF_DAY ), WorkCalendar.get( Calendar.MINUTE ), WorkCalendar.get( Calendar.SECOND ),
											WorkCalendar.get( Calendar.MILLISECOND ) ) ;
		}
		DebugString += String.format( FormatString, FormatArgument ) + "\r\n" ;

		Log.d( "DebugLog", DebugString ) ;

		try {
			WorkBufferedWriter = new BufferedWriter( new FileWriter( m_FileName, true ) ) ;
		} catch (IOException e) {
			// TODO 自動生�??�れ??catch ブロック
			e.printStackTrace();
		}
		if( WorkBufferedWriter != null )
		{
			try {
				WorkBufferedWriter.write( DebugString ) ;
			} catch (IOException e) {
				// TODO 自動生�??�れ??catch ブロック
				e.printStackTrace();
			}
			try {
				WorkBufferedWriter.close() ;
			} catch (IOException e) {
				// TODO 自動生�??�れ??catch ブロック
				e.printStackTrace();
			}
		}

		return( DebugString ) ;
	}


	public	void	ClearLogDateTime()
	{
		m_Calendar = null ;
	}


	public	long	GetLogDateTime()
	{
		return( m_Calendar == null ? 0 : m_Calendar.getTimeInMillis() ) ;
	}


	public	void	SetLogDateTime( long LogDateTime )
	{
		if( m_Calendar == null )
		{
			m_Calendar = Calendar.getInstance() ;
		}
		m_Calendar.setTimeInMillis( LogDateTime ) ;
	}


	public	boolean	Exist()
	{
		return( new File( m_FileName ).exists() ) ;
	}


	public	boolean	Delete()
	{
		File			WorkFile = new File( m_FileName ) ;
		boolean			Ret = true ;


		if( WorkFile.exists() )
		{
			Ret = WorkFile.delete() ;
		}
		return( Ret ) ;
	}
}
