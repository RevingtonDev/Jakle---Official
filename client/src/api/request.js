import { API_ROUTES } from "./routes";

const add_params = (url, params) => {
  if (!params) return url;
  return url + "?" + new URLSearchParams(params).toString();
};

const headers = (method, body, content) => {
  let header_content = {
    method: method,
    credentials: "include",
  };

  if (body) header_content["body"] = body;
  if (content) header_content["headers"] = {
    "Content-Type": content
  }

  return header_content;
};

export const request = (url, method, params, body, content) => {
  url = add_params(url, params);

  return fetch(url, headers(method, body, content === null ? undefined : "application/json"));
};


const xhr_request = async (method, url, params, headers, body, onload, onprogress) => {
  url = add_params(url, params);

  let xhr = new XMLHttpRequest();
  if (headers) {
    for (let header in headers) {
      xhr.setRequestHeader(header.name, header.value);
    }
  }
  xhr.open(method, url, true);
  xhr.onprogress = onprogress;
  xhr.onload = onload;
  xhr.send(body);
  return await xhr.response;
}

const handle_response = async (res) => {
  if (res && res.ok) return res.json();
  else return null;
};

const get_friends = async () => {
  return handle_response(
    await request(API_ROUTES.FRIENDS, "post", { active: "all" })
  );
};
const add_friend = async (id) => {
  return handle_response(await request(API_ROUTES.FRIEND, "put", { id: id }));
};
const respond_to_request = async (id, res) => {
  return handle_response(
    await request(API_ROUTES.FRIEND, "post", { id: id, add: res })
  );
};
const delete_request = async (id) => {
  return handle_response(
    await request(API_ROUTES.FRIEND, "delete", { id: id })
  );
};
const remove_friend = async (id) => {
  return handle_response(
    await request(API_ROUTES.FRIENDS, "delete", { id: id })
  );
};

const delete_messages = async (id) => {
  return handle_response(
    await request(API_ROUTES.MESSAGES, "delete", { id: id })
  );
};
const get_messages = async () => {
  return handle_response(await request(API_ROUTES.MESSAGES, "post"));
};
const get_info = async (id) => {
  return handle_response(await request(API_ROUTES.INFO, "post", { id: id }));
};
const discover_people = async (query, page, limit) => {
  return handle_response(
    await request(API_ROUTES.DISCOVER, "post", {
      query: query,
      page: page,
      limit: limit,
    })
  );
};
const get_notifications = async (all, page, limit, filter) => {
  if (all)
    return handle_response(
      await request(API_ROUTES.NOTIFICATIONS, "post", {
        all: all,
        page: page,
        limit: limit,
        filter: filter,
      })
    );
  else
    return await handle_response(
      await request(API_ROUTES.NOTIFICATIONS, "post")
    );
};

const update_account = async (body) => {
  return handle_response(
    await request(API_ROUTES.UPDATE, "post", undefined, body)
  );
};

const reset_password = async () => {
  return handle_response(await request(API_ROUTES.RESET, "post"));
};

const activate = async (email) => {
  await handle_response(request(API_ROUTES.ACTIVATE, "post", { email: email }));
};


const upload_file = async (body, onload, onprogress) => {
  return await xhr_request("post", API_ROUTES.UPLOAD,undefined, undefined, body, onload, onprogress);
}

export {
  get_friends,
  delete_messages,
  get_messages,
  remove_friend,
  add_friend,
  discover_people,
  respond_to_request,
  delete_request,
  get_info,
  get_notifications,
  update_account,
  reset_password,
  activate,
    upload_file,
};
