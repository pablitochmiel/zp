package edu.agh.zp.controller;

import edu.agh.zp.objects.*;
import edu.agh.zp.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping ( value = { "/glosowania" } )
public class GlosowaniaController {

	@Autowired
	VotingRepository votingRepository;

	@Autowired
	OptionSetRepository optionSetSession;

	@Autowired
	OptionRepository optionSession;

	@Autowired
	SetRepository setSession;

	@Autowired
	VotingTimerRepository votingTimerSession;

	@Autowired
	VotingControlRepository votingControlSession;


	@GetMapping ( value = { "" } )
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView( );
		modelAndView.setViewName( "glosowania" );
		return modelAndView;
	}

	@GetMapping ( value = { "/prezydenckie/plan" } )
	public ModelAndView prezydentForm() {
		deleteOldVotingData( java.sql.Date.valueOf( LocalDate.now( ) ) );
		ModelAndView modelAndView = new ModelAndView( );
		modelAndView.addObject( "ErrorList", null );
		modelAndView.setViewName( "presidentVotingAdd" );
		return modelAndView;
	}

	@PostMapping ( value = { "/prezydenckie/planAdd" } )
	public ModelAndView prezydentSubmit( @RequestParam Map< String, String > reqParameters ) throws ParseException {
		ModelAndView modelAndView = new ModelAndView( );
		modelAndView.setViewName( "glosowania" );
		String data = reqParameters.remove( "date" );
		SetEntity set = new SetEntity( "Wybory Prezydenckie " + data );
		LocalDate time = timeVerify( data, 7 );
		if ( time == null || reqParameters.size( ) < 3 || reqParameters.containsValue( "" ) ) {
			ArrayList< String > errors = errorsMsg( time, 7, reqParameters );
			modelAndView.setViewName( "presidentVotingAdd" );
			modelAndView.addObject( "ErrorList", errors );
			return modelAndView;
		}
		setSession.save( set );
		VotingEntity voting = new VotingEntity( java.sql.Date.valueOf( time ), java.sql.Time.valueOf( LocalTime.parse( "06:00:00" ) ), java.sql.Time.valueOf( LocalTime.parse( "21:00:00" ) ), set, null, VotingEntity.TypeOfVoting.PREZYDENT, "Wybory Prezydenckie " + data );
		votingRepository.save( voting );
		votingTimerSession.save( new VotingTimerEntity( voting.getVotingID( ), java.sql.Date.valueOf( time ) ) );
		for ( Map.Entry< String, String > entry : reqParameters.entrySet( ) ) {
			if ( entry.getKey( ).equals( "_csrf" ) )
				continue;
			OptionEntity option = new OptionEntity( entry.getValue( ) );
			optionSession.save( option );
			optionSetSession.save( new OptionSetEntity( option, set ) );
		}
		return modelAndView;
	}

	@GetMapping ( value = { "/referendum/plan" } )
	public ModelAndView referendumForm() {
		deleteOldVotingData( java.sql.Date.valueOf( LocalDate.now( ) ) );
		ModelAndView modelAndView = new ModelAndView( );
		modelAndView.addObject( "ErrorList", null );
		modelAndView.setViewName( "referendumAdd" );
		return modelAndView;
	}

	@PostMapping ( value = { "/referendum/planAdd" } )
	public ModelAndView referendumSubmit( @RequestParam Map< String, String > reqParameters ) throws ParseException {
		ModelAndView modelAndView = new ModelAndView( );
		modelAndView.setViewName( "glosowania" );
		String data = reqParameters.remove( "date" );
		LocalDate time = timeVerify( data, 7 );
		String desc = reqParameters.remove( "desc" );
		if ( time == null || desc.isEmpty( ) ) {
			ArrayList< String > errors = new ArrayList< String >( );
			if ( time == null )
				errors.add( "Wydarzenie musi być zaplanowane z 7 dniowym wyprzedzeniem\n" );
			if ( desc.isEmpty( ) )
				errors.add( "Należy wpisać treść pytania\n" );
			modelAndView.setViewName( "referendumAdd" );
			modelAndView.addObject( "ErrorList", errors );
			return modelAndView;
		}
		VotingEntity voting = new VotingEntity( java.sql.Date.valueOf( time ), java.sql.Time.valueOf( LocalTime.parse( "06:00:00" ) ), java.sql.Time.valueOf( LocalTime.parse( "21:00:00" ) ), setSession.findById( 1L ).get( ), null, VotingEntity.TypeOfVoting.REFERENDUM, desc );
		votingRepository.save( voting );
		votingTimerSession.save( new VotingTimerEntity( voting.getVotingID( ), java.sql.Date.valueOf( time ) ) );
		return modelAndView;
	}

	public LocalDate timeVerify( String time, int delay ) {
		if ( time.isEmpty( ) )
			return null;
		LocalDate now = java.time.LocalDate.now( );
		now = now.plusDays( delay );
		LocalDate res = LocalDate.parse( time );
		if ( now.isAfter( res ) )
			return null;
		return res;
	}

	public ArrayList< String > errorsMsg( LocalDate date, int delay, Map< String, String > param ) {
		ArrayList< String > res = new ArrayList< String >( );
		if ( date == null ) {
			res.add( "wydarzenie musi być zaplanowane z " + delay + " dniowym wyprzedzeniem\n" );
		}
		if ( param.size( ) < 3 ) {
			res.add( "musisz podać przynajmnej 2 kandydatów\n" );
		}
		if ( param.containsValue( "" ) ) {
			res.add( "dane kandydata nie mogą być puste\n" );
		}
		return res;
	}

	public void deleteOldVotingData( Date time ) {
		List< VotingTimerEntity > list = votingTimerSession.findByEraseBefore( time );
		if ( list.isEmpty( ) )
			return;
		for ( VotingTimerEntity Timer : list ) {
			votingControlSession.deleteByVotingID( votingRepository.findByVotingID( Timer.getVotingID( ) ) );
			votingTimerSession.delete( Timer );
		}
	}

	@GetMapping ( value = { "/zmianaDaty/{id}" } )
	public Object referendumDateChange( @RequestParam ( value = "dateForm", required = false ) Date dateForm, @PathVariable long id ) {
		VotingEntity voting = votingRepository.findByVotingID( id );

		if ( voting == null ) {
			throw new ResponseStatusException( HttpStatus.NOT_FOUND, "Voting not found" );
		}
		if ( voting.getVotingType( ) != VotingEntity.TypeOfVoting.REFERENDUM && voting.getVotingType( ) != VotingEntity.TypeOfVoting.PREZYDENT ) {
			throw new ResponseStatusException( HttpStatus.NOT_FOUND, "Wrong voting type" );
		}

		ModelAndView model = new ModelAndView( );

		if ( voting.getVotingType( ) == VotingEntity.TypeOfVoting.REFERENDUM ) {
			model.addObject( "heading", "Zmiana terminu referendum" );
			model.addObject( "name", "Referendum:" + voting.getVotingDescription( ) );
		}
		if ( voting.getVotingType( ) == VotingEntity.TypeOfVoting.PREZYDENT ) {
			model.addObject( "heading", "Zmiana terminu wyborów" );
			model.addObject( "name", "Wybory prezydenckie" );
		}

		String error = null;
		if ( dateForm != null ) {
			java.util.Date dateNow = new java.util.Date( );

			if ( dateForm.before( dateNow ) )
				error = "Głosowanie może być najwcześniej za 7 dni";
			model.addObject( "error", error );
		}

		//Wyslano zadanie zmiany daty, nie ma errora, zmieniamy
		if ( error == null && dateForm != null ) {
			voting.setVotingDate( dateForm );
			votingRepository.save( voting );

			RedirectView redirect = new RedirectView( );
			redirect.setUrl( "/kalendarz/wydarzenie/" + id );
			return redirect;
		}

		Date date = voting.getVotingDate( );
		String pattern = "dd MMMMM yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( pattern, new Locale( "pl", "PL" ) );
		String formattedDate = simpleDateFormat.format( date );

		model.addObject( "currentDate", formattedDate );
		model.addObject( "refName", voting.getVotingDescription( ) );
		model.setViewName( "changeEventDate/changePresRef" );
		return model;
	}

}


