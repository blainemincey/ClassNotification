function getStudentTemplate() {
	$.ajax({
        url: "tmpl/student.tmpl",
        dataType: "html",
        success: function( data ) {
            $( "head" ).append( data );
            updateStudentTable();
        }
    });
}

function getCancelClassTemplate() {
	$.ajax({
        url: "tmpl/cancelClass.tmpl",
        dataType: "html",
        success: function( data ) {
            $( "head" ).append( data );
            updateCancelClassTable();
        }
    });
}

function buildStudentRows(students) {
	return _.template( $( "#student-tmpl" ).html(), {"students": students});
}

function buildCancelClassRows(students) {
	return _.template( $( "#cancelClass-tmpl" ).html(), {"students": students});
}

function updateStudentTable() {
   $.ajax({
	   url: "rest/students/json",
	   cache: false,
	   success: function(data) {
            $('#members').empty().append(buildStudentRows(data));
       },
       error: function(error) {
            //console.log("error updating table -" + error.status);
       }
   });
}

function updateCancelClassTable() {
	   $.ajax({
		   url: "rest/students/json",
		   cache: false,
		   success: function(data) {
	            $('#members').empty().append(buildCancelClassRows(data));
	       },
	       error: function(error) {
	            //console.log("error updating table -" + error.status);
	       }
	   });
	}

function registerStudent(formValues) {
   //clear existing  msgs
   $('span.invalid').remove();
   $('span.success').remove();

   $.post('rest/students', formValues,
         function(data) {
            //console.log("Member registered");

            //clear input fields
            $('#reg')[0].reset();

            //mark success on the registration form
            $('#formMsgs').append($('<span class="success">Student Registered</span>'));

            updateStudentTable();
         }).error(function(error) {
            if ((error.status == 409) || (error.status == 400)) {
               //console.log("Validation error registering user!");

               var errorMsg = $.parseJSON(error.responseText);

               $.each(errorMsg, function(index, val){
                  $('<span class="invalid">' + val + '</span>')
                        .insertAfter($('#' + index));
               });
            } else {
               //console.log("error - unknown server issue");
               $('#formMsgs').append($('<span class="invalid">Unknown server error</span>'));
            }
         });
}


function touchScrollX(id)
{
  if (Modernizr.touch) {
        var el=document.querySelector(id);
        var scrollStartPos=0;

        el.addEventListener("touchstart", function(event) {
            scrollStartPos=this.scrollLeft+event.touches[0].pageX;
            event.preventDefault();
        },false);

        el.addEventListener("touchmove", function(event) {
            this.scrollLeft=scrollStartPos-event.touches[0].pageX;
            event.preventDefault();
        },false);
  }
}

function cancelClass(formValues)
{
	var myFormValues = formValues;
	var split = formValues.split('=');
	var className = split[1];
	
	//clear existing  msgs
	   $('span.invalid').remove();
	   $('span.success').remove();

	   $.post('rest/students/cancelClass', formValues,
	         function(data) {
	            //console.log("Member registered");

	            //clear input fields
	            $('#reg')[0].reset();

	            //mark success on the registration form
	            $('#formMsgs').append($('<span class="success">' + className + ' class has been cancelled!</span>'));

	            updateCancelClassTable();
	         }).error(function(error) {
	            if ((error.status == 409) || (error.status == 400)) {
	               //console.log("Validation error registering user!");

	               var errorMsg = $.parseJSON(error.responseText);

	               $.each(errorMsg, function(index, val){
	                  $('<span class="invalid">' + val + '</span>')
	                        .insertAfter($('#' + index));
	               });
	            } else {
	               //console.log("error - unknown server issue");
	               $('#formMsgs').append($('<span class="invalid">Unknown server error</span>'));
	            }
	         });
}
  
 