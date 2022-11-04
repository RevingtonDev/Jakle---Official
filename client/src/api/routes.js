const API = "/api/v1/";
const SOCIAL = API + "social/";

export const API_ROUTES = {
  NOTIFICATIONS: API + "notifications",
  INFO: API + "info",
  UPDATE: API + "update",
  RESET: API + "reset-link",
  ACTIVATE: API + "activate",
  UPLOAD: API + "upload",
  FRIENDS: SOCIAL + "friends",
  FRIEND: SOCIAL + "friend",
  SEARCH: SOCIAL + "search",
  MESSAGES: SOCIAL + "messages",
  DISCOVER: SOCIAL + "discover",
};

export const ENDPOINTS = {
  CONNECT: "/api/connect",
  SUBSCRIBE: "/user/app/queue",
  MESSAGE: "/api/message",
  NOTIFY: "/api/notify",
  TYPING: "/api/typing",
  OPEN: "/api/open",
};
