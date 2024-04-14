 const stripe = Stripe('pk_test_51Os4ALLtIo9b0kyinjcyYTobIOsRYvjQxgT4rrY3WEIlko8wayHS8harljyjHVOVycgNhTTA5h92RlEecvWXJV0500onauuJv1');
 const paymentButton = document.querySelector('#paymentButton');
 
 paymentButton.addEventListener('click', () => {
   stripe.redirectToCheckout({
     sessionId: sessionId
   })
 });