 //   document.getElementById("confirm").addEventListener("click", submitDecision('confirm'));
//document.getElementById("deny").addEventListener("click", submitDecision('deny'));
$( document ).ready(function() {
$("#confirm").click(function() {
  submitDecision('confirm');
});
$("#deny").click(function() {
  submitDecision('deny');
});
function submitDecision(decision) {
        alert(decision);
      if (decision === 'confirm') {
        alert('You confirmed :)');
      } else if (decision === 'deny') {
        alert('You denied :(');
      }
    }
});