import React, { Component } from "react";
import { useDispatch } from "react-redux";
import { Link } from "react-router-dom";
import { add_friend, delete_request, respond_to_request } from "../api/request";
import { execute } from "../store";
import {
  CheckCircle,
  PersonCheck,
  PersonHeart,
  PlusCircle,
  XCircle,
} from "./Images";

export const Profile = (props) => {
  return <ProfileComponent {...props} dispatch={useDispatch()} />;
};

class ProfileComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isWaiting: false,
    };
  }

  async addFriend(id) {
    if (id) {
      this.setState({ isWaiting: true });
      const req = await add_friend(id);
      if (req) {
        console.log(req);
        this.props.changeData(id, "type", "requested");
      }
      this.setState({ isWaiting: false });
    }
  }

  async removeRequest(id) {
    if (id) {
      this.setState({ isWaiting: true });
      const req = await delete_request(id);
      if (req) {
        console.log(req);
        this.props.changeData(id, "type", "stranger");
      }
      this.setState({ isWaiting: false });
    }
  }

  async respondToRequest(id, res) {
    if (id) {
      this.setState({ isWaiting: true });
      const req = await respond_to_request(id, res);
      if (req) {
        if (res === "yes") {
          this.props.changeData(id, "type", "friend");
          this.props.dispatch(
            execute({ type: "add_friend", payload: req.results })
          );
        } else this.props.changeData(id, "type", "stranger");
      }
      this.setState({ isWaiting: false });
    }
  }

  render() {
    const { type, user } = this.props;

    if (type === 0) {
      return (
        <>
          <div className="row-profile dummy-profile row center row-flex justify-start">
            <div className="row-logo dummy"></div>
            <div className="row-info center column-flex space-bet align-start">
              <div className="row-name dummy capsule"></div>
              <div className="row-activity dummy capsule"></div>
            </div>
          </div>
        </>
      );
    }

    if (type === 1) {
      const isTyping = user.typing;
      let time = "";
      if (user.time) {
        let now = new Date(),
          then = new Date(user.time);

        if (
          then - now < 1000 * 60 * 60 * 24 * 2 &&
          then.getDate() === now.getDate()
        ) {
          time = `Today at ${
            then.getHours() < 10 ? "0" + then.getHours() : then.getHours()
          }:${
            then.getMinutes() < 10 ? "0" + then.getMinutes() : then.getMinutes()
          }`;
        } else if (
          then - now < 1000 * 60 * 60 * 24 * 2 &&
          then.getDate() === now.getDate() - 1
        ) {
          time = `Yesterday at ${
            then.getHours() < 10 ? "0" + then.getHours() : then.getHours()
          }:${
            then.getMinutes() < 10 ? "0" + then.getMinutes() : then.getMinutes()
          }`;
        } else {
          time = `${then.toLocaleDateString()} at ${
            then.getHours() < 10 ? "0" + then.getHours() : then.getHours()
          }:${
            then.getMinutes() < 10 ? "0" + then.getMinutes() : then.getMinutes()
          }`;
        }
      }
      return (
        <>
          <Link
            className="row row-profile center row-flex justift-start"
            to={user.id}
            replace={true}
          >
            <div
              className="row-logo center"
              style={!user.profilePic ? { padding: "8px" } : {}}
            >
              {user && user.profilePic ? (
                <img src={user.profilePic} alt="profile-pic" />
              ) : (
                PersonHeart
              )}
              {user.activity && !user.time ? (
                <>
                  <span className="status online"></span>
                </>
              ) : (
                ""
              )}
            </div>
            <div className="row-info center column-flex space-bet align-start">
              <div className="row-name capsule">
                {user && user.name ? user.name : ""}
              </div>
              {(user.activity || user.time) && (
                <div
                  className={
                    "row-activity capsule " +
                    (user && user.time ? "last-seen" : "online")
                  }
                >
                  {user.time
                    ? "Last seen " + time
                    : isTyping
                    ? "Typing..."
                    : "Online"}
                </div>
              )}
            </div>
            {user.count ? (
              <>
                <div className="unread-count center">{user.count}</div>
              </>
            ) : (
              ""
            )}
          </Link>
        </>
      );
    }

    const { isWaiting } = this.state;
    let action;
    switch (user.type) {
      case "friend":
        action = (
          <>
            <span className="info-logo center">{PersonCheck}</span>
          </>
        );
        break;
      case "requested":
        action = (
          <>
            <button
              className="btn icon-d-btn red center row-flex capsule"
              disabled={isWaiting}
              onClick={() => {
                this.removeRequest(user.id);
              }}
            >
              <span>{XCircle}</span>
              Remove Friend Request
            </button>
          </>
        );
        break;
      case "pending":
        action = (
          <>
            <button
              className="btn icon-d-btn green center row-flex capsule"
              disabled={isWaiting}
              onClick={() => {
                this.respondToRequest(user.id, "yes");
              }}
            >
              <span>{CheckCircle}</span> Accept
            </button>

            <button
              className="btn icon-d-btn red center row-flex capsule"
              disabled={isWaiting}
              onClick={() => {
                this.respondToRequest(user.id, "no");
              }}
            >
              <span>{XCircle}</span>
              Decline
            </button>
          </>
        );
        break;
      case "stranger":
        action = (
          <>
            <button
              className="btn icon-d-btn blue center row-flex capsule"
              disabled={isWaiting}
              onClick={() => {
                this.addFriend(user.id);
              }}
            >
              <span>{PlusCircle}</span>
              Add Friend
            </button>
          </>
        );
        break;
      default:
        break;
    }
    return (
      <>
        <div className="center column-flex profile center">
          {isWaiting && (
            <>
              <div
                className="loading-container center parent-fill"
                style={{
                  backgroundColor: "var(--def-color)",
                  position: "absolute",
                }}
              >
                <div className="component-waiting center space-bet align-start column-flex">
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                </div>
              </div>
            </>
          )}
          <div
            className="row-logo center row"
            style={!user.profilePic ? { padding: "8px" } : {}}
          >
            {user && user.profilePic ? (
              <img src={user.profilePic} alt="profile-pic" />
            ) : (
              PersonHeart
            )}
          </div>
          <div className="name center space-bet align-start">
            <div className="capsule">{user && user.name}</div>
          </div>
          <div className="control center row-flex">{action}</div>
        </div>
      </>
    );
  }
}
