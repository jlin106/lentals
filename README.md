# lentals

People inevitably have a few random items they need every week that they don’t have on hand. This is because rarely does anyone purchase seldomly used items. Even then it is usually impossible for them to get it immediately.

Lentals provides a hassle-free, cheap, and convenient alternative wherein a user can simply borrow the items they need!

Developers: Jennifer Lin, Manan Aggarwal, Cameron Franz, Reem Larabi

Video Demo: https://youtu.be/4T5ZSdFHQSw 

## Sprint 1
**Features:**
- Facebook integration (Facebook log-in and populating navigation drawer header with user info)
- Firebase authentication for user log-in
- Firebase database to store items
- Navigation Drawer and drawer elements (Main Listings, My Items, Log out)
- Floating Add Button - Add Item Listing functionality

**Summary:**

Currently, users are able to log in using their Facebook account. Once the user logs in, the main listings fragment will populate the screen. The user is able to scroll through listings and add an item listing using the floating add button. There is currently no search or filter functionality. There is also only the listview, no map view. The navigation drawer has a header with the user's Facebook profile picture and their name. There is currently no edit profile button in the header. The navigation drawer also has menu items of Main Listings, My Items and Log out. The My Items screen launches a fragment where the user's posted items populate the screen. Currently, just Listed items are shown, there is no tab for Borrowed items. There is a floating add button in the main listings and my items fragments that will launch the Add Item fragment. Here the user can input the item name, price, a description and an image. If the user clicks cancel, they are directed back to their previous screen. If the user clicks post, the item is added to the item database. Currently there are no item profile screens so users can't click on an item listing for more info just yet. The user can log out using the log out menu item in the navigation drawer.

## Sprint 2
**Features:**
- Chat/Messages - decided against Facebook messenger (would need additional permissions and such)
- Updated Main Listings Screen with basic search
    - Query Firestore Firebase database entries, save history locally
- Item Profile Screen - use unique document ID to get data from the Firebase database on the specific item and populate the screen
    - Also added features to allow users to favorite items (star icon) and message the person who listed item
- Updated My Items Screen with editing abilities
    - User can edit, delete or change visibility of listed items
- Favorited Items Screen 
    - Populated with a user's favorited items
- User Profile Screen
    - Displays certain user's name & picture & listed items as well as a message button
- Maps Screen
    - Displays the location of items on the marketplace
    - Clicking on the photo icons will display distance of the item from your current location
- Backend: Optimized picture uploading/storing and backstack

**Summary:**
Going off of our app from the first sprint, we have optimized and added a lot of more functionality. The way we were previouslt storing our item images in the database prevented users from uploading images of a certain quality and led to a very long loading time when launching main listings and other screens. We have changed the way we store images to allow much faster and more efficient uploading and storing. We have also cleaned up the backstack as we rely on the navigation drawer and the Android back button for navigation. Although we wanted to use a ViewPager to allow the user to swipe between Main Listings vs Maps and Favorited Items vs Listed Items, currently all of these screens are accessed through the Navigation Drawer. We hope to incorporate the swipe view in the future. Though we implemented a search function, the search is a direct string comparison so note that the search is very basic as it only checks for substring matching, including capitalization of the word! Instead of launching the Facebook messenger app upon clicking on the Message button on user profiles and item profiles, we created our own chat activity. The other thing we wanted to note was the Maps Screen. Right now the maps feature allows the user to see where items are around their current location and when the user clicks on a photo icon representing an item, it shows the name as well as how far the item is from the user's current location. In the future, we hope to allow the user to double click or long tap or some way open the item profile for the selected item to allow more functionality. Although we did not get to implement some features such as ratings/reviews or filtering, we've really enjoyed working on the app thus far!

**Testing:** @Simon, Log in with your own Facebook account and test out the app!
