#droid-fastmailfm-contacts

Simple read-only access to your Fastmail.fm address book from your Android contacts/people app. Something basic to hold me over until they implement proper CardDAV support.

##Download

You can build it from source, or I can save you the hassle if you buy it on the Play Store for $0.99. [Here's the link](https://play.google.com/store/apps/details?id=com.rudism.fastmail.fm.contacts).

##Usage

1. Create a new *Fastmail.fm Contacts* account from your Android's account settings screen. You can also load up the *Fastmail.fm Contacts* app for a shortcut to the *Add New Account* screen.

2. Enter your Fastmail.fm credentials (your email address and password).

3. Enjoy your new address book.

##Notes

- The sync adapter uses Fastmail.fm's LDAP API for read-only access to your address book. Changes you make on Fastmail.fm should sync to your phone within a day, but you will not be able to make changes from your phone and have them sync the other direction.

- If you don't want to wait a whole day, or you disabled the auto-sync checkbox when creating the account, you can manually sync from the account screen in your Android settings at any time.

- The fields that are synced are Name, Company, up to two Emails, Home phone, Mobile phone, Work phone, and Notes. Address could be added if enough people want it. Unfortunately birthdays and anniversaries are not available via LDAP so those can never be synced with this app.

- **This is highly untested software. There are probably bugs. Feel free to report issues here, especially if you paid for the app. I'll fix bugs as quickly as I can.**
