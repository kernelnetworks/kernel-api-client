**Kernel Flat file extract REST API documentation**


**Overview:**

Kernel can enable REST API access to all data for its customers. The REST API may be used to pull all existing interactions, surveys, and insights data. The data pull is secure (e.g. authenticated system user account over https connection) so that only customer's approved systems have access. We will also provide and update this document explaining the data input and outputs through the exposed API functions.

To use the Kernel REST APIs, **customers will need to build a REST API client.** This API client will first need to make a call to fetch a valid auth token. Then make all subsequent calls using that valid auth token to retrieve the requested data via the flat file csv extract endpoint. Lastly, customers will need to open the csv file and import its contents into the storage system of your choice. The columns in the csv export can be configured for inclusion/exclusion per customer's requirements, by default Kernel returns all available columns. In addition, these columns are not expected to change (order or names) as you continue to use the Kernel Flat file extract API.

The same flat file CSV extract can be downloaded from the Kernel UI at this page: [https://<customer>.gokernel.com/report/csvExport](https://<customer>.gokernel.com/report/csvExport). Kernel users can freely export the data in Kernel at any time, but are limited to exporting data their user has visibility to in Kernel which is based on their user role and team assignments.

**System access:**

- Kernel system API user email: [<customer>-api@gokernel.com](mailto:<customer>-api@gokernel.com)
- Password: *TBD*

Customer systems will call the authorization function with this user's credentials to securely authenticate to Kernel for access to customer's survey/insights data.

**API changes/updates:**

- The email list "[<customer>-api@gokernel.com](mailto:<customer>-api@gokernel.com)" will be created to inform customer employees of changes to the integration or APIs that may impact their dashboard/data warehouse integration.
- The members of this email list are: **TBD**

**API Overview**

The Kernel APIs are accessed from a set of REST calls at this base address:

[https://<customer>.gokernel.com/kernelAPI/v2](https://<customer>.gokernel.com/kernelAPI/v2)/ and by postpending the specific endpoint to be called to the base URL. Your API client will need to successfully make the calls below to retrieve the csv file.

**High-level call flow:**

1. Call **POST user/authorize** to get Kernel API system user auth token.
2. Call **POST storage/topicExportAsCSV** to get the csv file and data.

The authorization token returned by the authorize function must be provided to subsequent calls in an "Authorization" HTTP header.

**API data**

- If a request sends JSON data, the Content Type should be "application/json". If a request sends form parameters, use the Content Type "application/x-[www-form-urlencoded](http://www.form-urlencoded/)".
- Responses will typically be JSON, the csv export is an exception as it is a text.
- All dates in API requests and responses are formatted as:
**yyyy-mm-dd hh:mm:ss.SSSZ**
and are in UTC timezone.
**Note:** white space date/time separator should be replaced with character **T** in all date values in request payload.

All examples in this document use **curl** utility for making HTTPS requests and getting responses.

The same SLA offered for the Kernel site is offered for the REST API. Call limits to the APIs should not exceed 1 per 10 seconds to avoid any performance impacts.

You may be able to discern additional API endpoints, or additional parameters for these calls from observation. Any such usage is unsupported and may change without notice.

## API descriptions

### Authorize

**POST user/authorize**

Get the Kernel authorization token with an email and password.

Form parameters in the request payload:

- email - the user's email
- password - the user's password

Headers:

- Content Type: application/x-www-form-urlencoded

Response Type: application/json

Response payload:

- JSON serialization of AuthorizationObject that contains user id, authorization token and its expiration attribute (see below a sample Java representation of this object). The "authorization" field value of this JSON should be sent as a value of "Authorization" HTTP header called in all subsequent requests.

class AuthorizationObject {

String userId;

String authorization;

Boolean expired;

}

Example request:

curl -X POST https://<customer>.gokernel.com/kernelAPI/v2/user/authorize --data 'email=<customer>-api@gokernel.com' --data 'password=TBD'

Example response:

{

"userId":"BD47BE08ECA511E1A8425404A6247829"

"authorization":"fbf5eaf3-5683-49bd-b2c6-f256a994d7e9",

"expired":false

}

Response HTTP Status codes:

- Created (201) - Successful response
- Bad Request (400) - Missing parameters
- Unauthorized (401) - Unable to obtain authorization (user does not exist, password invalid)
- Forbidden (403) - User is deactivated

### Export interactions (with insights and survey responses)

**POST storage/topicExportAsCSV?[short=true][&types=TYPE1[&types=TYPE2…]]**

where:

short is an optional parameter, when specified and true, the CVS report is produced in short form (fewer columns).

types is one or more of the following types: **Insights** , **InitiativeResponses** , **Feedbacks**. If not specified the report will contain all types. When **Insights** type is specified the response contains initiative insights. When **InitiativeResponses** type is specified the response contains responses to initiative questions (survey results). When **Feedbacks** type is specified the response contains interactions that have neither insights nor responses. If types parameter is not specified the response contains all interactions that contain either insights or responses or both.

Request data contains filters and is expected as JSON serialization of the FilterObject containing common filters that will be applied when response data is retrieved from the database (see below a sample Java representation of this object). One or several filters can be included in the FilterObject. If no FilterObject is specified in the request, the response will contain all interactions.

The response data is in CSV format, and sent with the content type text/csv.

Response HTTP Status codes:

- OK (200) - Successful response
- Unauthorized (401) - Unable to obtain authorization (user does not exist, password invalid)
- Forbidden (403) - User does not have permission for this action

Filters:

class FilterObject {

java.sql.Timestamp startDate; // earliest "Interaction Date"

java.sql.Timestamp endDate; // latest "Interaction Date"

java.util.Set\<String\> contactReputations; // contact recognition level

java.util.Set\<String\> contactRoles; // contact primary roles

java.util.Set\<String\> institutionTypes; // institution types

java.util.Set\<String\> provinces; // province codes

java.util.Set\<String\> countries; // country codes

}

Example:

_curl -X POST__"[https://<customer>.gokernel.com/kernelAPI/v2/storage/topicExportAsCSV](https://<customer>.gokernel.com/kernelAPI/v2/storage/topicExportAsCSV)?types=Insights&types=InitiativeResponses" --header 'Content-Type: application/json' --header 'Authorization: de4f1237-1944-4274-a1e6-67c2bdc451ad' -d '{_"startDate":"2020-08-10T07:00:00.000Z","endDate":"2020-11-11T07:59:59.999Z"_}'_

In the above example, to constrain the report by specific dates we specified start and end dates for all interactions that should be contained in the response report.

**Notes:**

- If this API endpoint is called by a user with access below Site Admin, then the output will be restricted to only the teams that they are a member of.
 Note: [<customer>-api@gokernel.com](mailto:<customer>-api@gokernel.com) user is a member of all teams.
- Some column names and values may vary depending on the customer's configuration. Some columns may be removed and additional columns of data may be added to the export upon request and Kernel engineering team's review.

**Report Data Descriptions:**

- Interaction ID - unique ID for Kernel's "Interaction" records. An interaction represents all the combined data for a given HCP meeting (i.e. contact, user/creator, initiatives, surveys, insights, and interaction date, these are all part of a single interaction in Kernel).
- Interaction External ID - unique ID for interactions that originated outside of Kernel either from a data import (if an external ID was provided in the source file) or an integration with a CRM system where interactions are created in CRM first then synced to Kernel.
- Interaction Submit Time - creation date of the interaction in Kernel.
- Interaction Date - date of the meeting/interaction with the HCP. This date is what all reports/filters in Kernel use. We do track creation dates (Interaction Submit Time column in the export) and last modified dates internally but for the purposes of reporting only the Interaction Dates are used.
- Time Spent - represents the amount of time in minutes the user spent with the HCP during their interaction. This is an optional field depending on the client's Kernel configuration.
- Interaction Type - represents the type of interaction (e.g. email, phone, web, in person, etc). This is an optional field depending on the client's Kernel configuration, list values are custom per Kernel instance based on client's requirements.
- Interaction Activity Type - represents the type of activity (e.g. reactive, proactive, etc). This is an optional field depending on the client's Kernel configuration, list values are custom per Kernel instance based on client's requirements.
- Interaction Meeting Reason - represents the reason for the interaction (e.g. HCP request, MedInfo request, Group presentation, scientific exchange, etc). This is an optional field depending on the client's Kernel configuration, list values are custom per Kernel instance based on client's requirements.
- Contact ID - unique internal ID representing the contact in Kernel. A contact in Kernel is typically a Health Care Practitioner (HCP), but may also include Researchers, Payers, etc.
- Contact - name of the contact.
- Recognition - represents the recognition level or KOL Tier assigned to the contact. The values are custom per the client's requirements and typically synchronized with the client's CRM.
- Role - represents the Role assigned to the contact. Some clients internally may refer to the Role as the HCP's specialty or job title. The values are custom per the client's requirements and typically synchronized with the client's CRM.
- Contact External ID - unique ID for contacts that originated outside of Kernel either from a data import (if an external ID was provided in the source file) or an integration with a CRM system where contacts/accounts are maintained in the CRM and synced to Kernel. **Typically with CRM integrations this ID aligns with the 18 Digit SFDC ID for the Account.**
- Credentials - represents a contacts credentials, e.g. MD, DO, Phd, etc. This list is client specific and often originates from the client's CRM.
- contactAttributeIds - unique IDs in Kernel representing various contact attributes.
- Contact Attributes - represents additional attributes about each contact that are used for filtering contacts/interactions data. The same contact attributes are used across contacts if it applies to that contact. This list is client specific and typically originates from CRM synchronizations with the HCP Account. Examples include: Medical KOL, Disease Area Focus, Principal investigator and Clinical Trial names.
- Institution ID - unique internal ID representing the institution in Kernel. An institution in Kernel represents the organization the contact works at, or where the interaction took place.
- Institution - name of the institution.
- Institution Type - represents the type of institution assigned to the institution. The values are custom per the client's requirements and typically synchronized with the client's CRM. Examples include: Academic, Community, and Research focused organizations.
- Institution External ID - unique ID for institutions that originated outside of Kernel either from a data import (if an external ID was provided in the source file) or an integration with a CRM system where institutions/accounts are maintained in the CRM and synced to Kernel. **Typically with CRM integrations this ID aligns with the 18 Digit SFDC ID for the organization account affiliated with the HCP account.**
- Street Address - address for the institution.
- City - City for the institution.
- State / Province - state or province for the institution.
- Country - country for the institution.
- Postal Code - postal or zip code for the institution.
- User - represents the display name for the user that created the data.
- User Email - represents the user's work email address for the user that created the data.
- Initiatives - represents the name of the initiative that the survey responses or insights belong to. Initiative names are unique in Kernel. An initiative can be used to collect survey responses, insights, or both. Insights can be assigned to more than one initiative, and every insight must have at least one assigned initiative. Surveys can only belong to a single initiative. Any number of teams can be granted access to an initiative which means users that belong to those teams see this initiative in the Kernel UI (for adding interactions data to and pulling reporting data for that initiative).
- Teams - names of teams with access to the initiative. Team names must be unique.
- topicTypeName - internal type value. Only "Initiative" types are used in the export.
- Question - poll question title. A poll question is optionally part of an initiative. Questions are not guaranteed to be unique across initiatives.
- pollPosition - represents the position or order of this poll question within the initiative. Kernel Admins can change a question's position overtime.
- Question Choice - represents the poll question answer choice label which users can choose from when capturing responses for an initiative with a poll question. If the poll is single select, then there will be only one value, if it's a multi-select poll there may be multiple values.
- pollChoiceIndex - represents the order or position of the question choice label as it appears to users in the UI among the other available question choices.
- pollChoiceType - represents the choice type which can either be "0" meaning a question choice with a defined label, or "1" meaning an Other write in option for users to write in their own answer.
- Other Response - for pollChoiceType = 1, this represents the free text written in by the user as their "other answer".
- Response Details - represents an optional field for single select or multi select polls where users can add additional free text details explaining why they selected a given question choice for the poll.
- Insight External ID - unique ID for insights that originated outside of Kernel either from a data import (if an external ID was provided in the source file) or an integration with a CRM system where insights are created in CRM first then synced to Kernel.
- Insight - free text of the insight entered by a user.
- Open Question - question title for an open ended question.
- questionPosition - position of open ended questions within an initiative.
- Open Answer - user's free text answer to open ended questions.
- Admin Tags - tags (keywords) created by client Administrators of the Kernel system and optionally assigned to a given insight.
- Kernel Tags - tags (keywords) created by Kernel's AI system and optionally assigned to a given insight.
