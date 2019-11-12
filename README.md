# lentals

People inevitably have a few random items they need every week that they don’t have on hand. This is because rarely does anyone purchase seldomly used items. Even then it is usually impossible for them to get it immediately.

Lentals provides a hassle-free, cheap, and convenient alternative wherein a user can simply borrow the items they need!

## Sprint 1
**Features:**
- Facebook integration (Facebook log-in and populating navigation drawer header with user info)
- Firebase authentication for user log-in
- Firebase database to store items
- Navigation Drawer and drawer elements (Main Listings, My Items, Log out)
- Floating Add Button - Add Item Listing functionality

**Summary:**

Currently, users are able to log in using their Facebook account. Once the user logs in, the main listings fragment will populate the screen. The user is able to scroll through listings and add an item listing using the floating add button. There is currently no search or filter functionality. There is also only the listview, no map view. The navigation drawer has a header with the user's Facebook profile picture and their name. There is currently no edit profile button in the header. The navigation drawer also has menu items of Main Listings, My Items and Log out. The My Items screen launches a fragment where the user's posted items populate the screen. Currently, just Listed items are shown, there is no tab for Borrowed items. There is a floating add button in the main listings and my items fragments that will launch the Add Item fragment. Here the user can input the item name, price, a description and an image. If the user clicks cancel, they are directed back to their previous screen. If the user clicks post, the item is added to the item database. Currently there are no item profile screens so users can't click on an item listing for more info just yet. The user can log out using the log out menu item in the navigation drawer.

**Testing:** @Simon, Log in to Facebook and add some items! Disclaimer: since you have no items at first, the My Items screen will be completely blank (we'll try to add a "You haven't added any items yet" message later because being completely blank might seem like the app doesn't work) but once you add an item, it should pop up as a listing. There should already be other items in Main Listings that the team has added!
