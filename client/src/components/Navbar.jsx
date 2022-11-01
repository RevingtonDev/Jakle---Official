import React, { Component } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useParams } from "react-router-dom";

import { Logo, Gear } from "./Images";

export const Navbar = (props) => {
  return (
    <NavbarComponent
      {...props}
      dispatch={useDispatch()}
      hasNewNotifications={
        useSelector((state) => state.reducer).hasNewNotifications
      }
      params={useParams()}
    />
  );
};

class NavbarComponent extends Component {
  constructor(props) {
    super(props);

    this.state = { isPanelShowing: false, isWaiting: false, notifications: 0 };
  }
  /*
    async changePanelState(state) {
        this.setState({ isPanelShowing: state });
        if (state) {
            this.props.dispatch(execute({ type: "notify", payload: false }));

            const req = await get_notifications();
            if (req)
                this.setState({ notifications: req.results });
            else
                this.setState({ notifications: 0 });
        }
    }
*/
  changePanelState(state) {
    this.setState({ isPanelShowing: state });
  }

  render() {
    const { hasNewNotifications, params } = this.props;
    const { isPanelShowing } = this.state;
    console.log(params);
    return (
      <>
        <button
          className={
            "nv-controller center space-bet column-flex " +
            (isPanelShowing ? "close" : "") +
            (params && JSON.stringify(params) !== "{}" ? " remove" : "")
          }
          onClick={() => {
            this.changePanelState(!isPanelShowing);
          }}
        >
          <span className="controller-bar"></span>
          <span className="controller-bar"></span>
          <span className="controller-bar"></span>
        </button>
        <nav
          className={
            "nv-bar center space-bet row-flex " +
            (!isPanelShowing ? "hide" : "")
          }
        >
          <div className="title nv-title center row-flex">
            <img
              className="title-logo"
              src={Logo}
              alt="logo"
              style={{ backgroundImage: "cover" }}
            />
            <ul className="nav-links center row-flex">
              <li className="nav-link">
                <Link
                  onClick={() => {
                    this.changePanelState(false);
                  }}
                  to={"/op/dashboard"}
                  className="link"
                >
                  Dashboard
                </Link>
              </li>
              <li className="nav-link">
                <Link
                  onClick={() => {
                    this.changePanelState(false);
                  }}
                  to={"/op/discover"}
                  className="link"
                >
                  Discover
                </Link>
              </li>
              <li className="nav-link">
                <Link
                  onClick={() => {
                    this.changePanelState(false);
                  }}
                  to={"/op/notifications"}
                  className={
                    "link rel " + (hasNewNotifications ? "notify" : "")
                  }
                >
                  Notifications
                </Link>
              </li>
              {/*
                            <li className="nav-link">
                                <Link to={"/op/profile"} className='link' >Profile</Link>
                            </li>
                            */}
            </ul>
          </div>
          <div className="nav-controlls center row-flex">
            <Link
              onClick={() => {
                this.changePanelState(false);
              }}
              to={"/op/settings"}
              replace={true}
              className="nv-btn center"
            >
              {Gear}
            </Link>
            <a href="/api/v1/logout" className="btn default-btn">
              Logout
            </a>
          </div>
        </nav>
      </>
    );
  }
}
